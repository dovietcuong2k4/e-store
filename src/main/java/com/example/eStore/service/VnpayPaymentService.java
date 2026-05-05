package com.example.eStore.service;

import com.example.eStore.config.VnpayProperties;
import com.example.eStore.dto.constants.Constants;
import com.example.eStore.dto.response.VnpayIpnResponse;
import com.example.eStore.dto.response.VnpayPaymentResultResponse;
import com.example.eStore.entity.Order;
import com.example.eStore.entity.OrderHistory;
import com.example.eStore.exception.AppException;
import com.example.eStore.repository.OrderHistoryRepository;
import com.example.eStore.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class VnpayPaymentService {
    private static final String VNPAY_SUCCESS_CODE = "00";
    private static final String SECURE_HASH = "vnp_SecureHash";
    private static final String SECURE_HASH_TYPE = "vnp_SecureHashType";
    private static final DateTimeFormatter VNPAY_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final VnpayProperties properties;
    private final OrderRepository orderRepository;
    private final OrderHistoryRepository orderHistoryRepository;

    public void ensureConfigured() {
        if (!StringUtils.hasText(properties.getTmnCode()) || !StringUtils.hasText(properties.getHashSecret())) {
            throw new AppException(
                    "VNPAY sandbox credentials are not configured",
                    Constants.ErrorCode.Payment.CONFIG_MISSING
            );
        }
    }

    public String createPaymentUrl(Order order, long amount, String bankCode, String clientIp) {
        ensureConfigured();
        if (amount <= 0) {
            throw new AppException(
                    "VNPAY amount must be greater than 0",
                    Constants.ErrorCode.Payment.INVALID_AMOUNT
            );
        }

        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", properties.getVersion());
        params.put("vnp_Command", properties.getCommand());
        params.put("vnp_TmnCode", properties.getTmnCode());
        params.put("vnp_Amount", BigDecimal.valueOf(amount).multiply(BigDecimal.valueOf(100)).toPlainString());
        params.put("vnp_CurrCode", properties.getCurrencyCode());
        params.put("vnp_TxnRef", order.getPaymentTxnRef());
        params.put("vnp_OrderInfo", "Thanh toan don hang " + order.getId());
        params.put("vnp_OrderType", properties.getOrderType());
        params.put("vnp_Locale", properties.getLocale());
        params.put("vnp_ReturnUrl", properties.getReturnUrl());
        params.put("vnp_IpAddr", StringUtils.hasText(clientIp) ? clientIp : "127.0.0.1");
        params.put("vnp_CreateDate", LocalDateTime.now(VIETNAM_ZONE).format(VNPAY_DATE_FORMAT));

        if (StringUtils.hasText(bankCode)) {
            params.put("vnp_BankCode", bankCode.trim());
        }

        String query = buildQueryString(params);
        String secureHash = hmacSha512(buildHashData(params), properties.getHashSecret());
        return properties.getPayUrl() + "?" + query + "&" + SECURE_HASH + "=" + secureHash;
    }

    @Transactional
    public VnpayPaymentResultResponse handleReturn(Map<String, String> params) {
        if (!isValidSignature(params)) {
            throw new AppException(
                    "Invalid VNPAY signature",
                    Constants.ErrorCode.Payment.INVALID_SIGNATURE
            );
        }

        Order order = findOrderFromParams(params);
        validateAmount(order, params);

        boolean paid = isSuccessTransaction(params);
        if (paid) {
            markPaymentPaid(order, params);
        } else {
            markPaymentFailed(order, params);
        }

        return buildPaymentResult(order, params, paid);
    }

    @Transactional
    public VnpayIpnResponse handleIpn(Map<String, String> params) {
        try {
            if (!isValidSignature(params)) {
                return new VnpayIpnResponse("97", "Invalid signature");
            }

            Order order = findOrderFromParams(params);
            validateAmount(order, params);

            if (!Constants.PaymentStatus.PENDING.equals(order.getPaymentStatus())) {
                return new VnpayIpnResponse("02", "Order already confirmed");
            }

            if (isSuccessTransaction(params)) {
                markPaymentPaid(order, params);
            } else {
                markPaymentFailed(order, params);
            }

            return new VnpayIpnResponse("00", "Confirm Success");
        } catch (AppException exception) {
            if (Constants.ErrorCode.Payment.ORDER_NOT_FOUND.equals(exception.getErrorCode())) {
                return new VnpayIpnResponse("01", "Order not found");
            }
            if (Constants.ErrorCode.Payment.INVALID_AMOUNT.equals(exception.getErrorCode())) {
                return new VnpayIpnResponse("04", "Invalid amount");
            }
            return new VnpayIpnResponse("99", "Unknown error");
        } catch (RuntimeException exception) {
            return new VnpayIpnResponse("99", "Unknown error");
        }
    }

    private boolean isValidSignature(Map<String, String> params) {
        String receivedHash = params.get(SECURE_HASH);
        if (!StringUtils.hasText(receivedHash)) {
            return false;
        }

        Map<String, String> signedFields = copySignedFields(params);
        String expectedHash = hmacSha512(buildHashData(signedFields), properties.getHashSecret());
        return expectedHash.equalsIgnoreCase(receivedHash);
    }

    private Order findOrderFromParams(Map<String, String> params) {
        String txnRef = params.get("vnp_TxnRef");
        try {
            Long orderId = Long.valueOf(txnRef);
            return orderRepository.findDetailedById(orderId)
                    .orElseThrow(() -> new AppException(
                            "Order not found",
                            Constants.ErrorCode.Payment.ORDER_NOT_FOUND
                    ));
        } catch (NumberFormatException exception) {
            throw new AppException(
                    "Invalid VNPAY transaction reference",
                    Constants.ErrorCode.Payment.INVALID_TRANSACTION
            );
        }
    }

    private void validateAmount(Order order, Map<String, String> params) {
        long expectedAmount = calculateOrderTotal(order) * 100;
        long actualAmount;
        try {
            actualAmount = Long.parseLong(params.getOrDefault("vnp_Amount", "-1"));
        } catch (NumberFormatException exception) {
            throw new AppException(
                    "Invalid VNPAY amount",
                    Constants.ErrorCode.Payment.INVALID_AMOUNT
            );
        }

        if (expectedAmount != actualAmount) {
            throw new AppException(
                    "VNPAY amount does not match order amount",
                    Constants.ErrorCode.Payment.INVALID_AMOUNT
            );
        }
    }

    private long calculateOrderTotal(Order order) {
        long subtotal = order.getOrderItems() == null
                ? 0L
                : order.getOrderItems()
                        .stream()
                        .mapToLong(item -> item.getPrice() * item.getQuantity())
                        .sum();
        long discount = order.getDiscountAmount() != null ? order.getDiscountAmount() : 0L;
        return Math.max(0L, subtotal - discount);
    }

    private boolean isSuccessTransaction(Map<String, String> params) {
        return VNPAY_SUCCESS_CODE.equals(params.get("vnp_ResponseCode"))
                && VNPAY_SUCCESS_CODE.equals(params.get("vnp_TransactionStatus"));
    }

    private void markPaymentPaid(Order order, Map<String, String> params) {
        if (Constants.PaymentStatus.PAID.equals(order.getPaymentStatus())) {
            return;
        }

        order.setPaymentStatus(Constants.PaymentStatus.PAID);
        fillPaymentGatewayFields(order, params);
        order.setPaymentCompletedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    private void markPaymentFailed(Order order, Map<String, String> params) {
        if (Constants.PaymentStatus.PAID.equals(order.getPaymentStatus())) {
            return;
        }

        String oldStatus = order.getStatus();
        order.setPaymentStatus(Constants.PaymentStatus.FAILED);
        fillPaymentGatewayFields(order, params);
        if (!Constants.OrderStatus.CANCELLED.equals(order.getStatus())) {
            order.setStatus(Constants.OrderStatus.CANCELLED);
        }
        orderRepository.save(order);

        if (!Constants.OrderStatus.CANCELLED.equals(oldStatus)) {
            orderHistoryRepository.save(OrderHistory.builder()
                    .order(order)
                    .fromStatus(oldStatus)
                    .toStatus(Constants.OrderStatus.CANCELLED)
                    .changedBy("VNPAY")
                    .changedAt(LocalDateTime.now())
                    .build());
        }
    }

    private void fillPaymentGatewayFields(Order order, Map<String, String> params) {
        order.setPaymentTransactionNo(params.get("vnp_TransactionNo"));
        order.setPaymentBankCode(params.get("vnp_BankCode"));
        order.setPaymentResponseCode(params.get("vnp_ResponseCode"));
        order.setPaymentTransactionStatus(params.get("vnp_TransactionStatus"));
        order.setPaymentPayDate(params.get("vnp_PayDate"));
    }

    private VnpayPaymentResultResponse buildPaymentResult(Order order, Map<String, String> params, boolean paid) {
        return VnpayPaymentResultResponse.builder()
                .orderId(order.getId())
                .paid(paid)
                .message(paid ? Constants.Message.Payment.VNPAY_RETURN_SUCCESS : Constants.Message.Payment.VNPAY_RETURN_FAILED)
                .responseCode(params.get("vnp_ResponseCode"))
                .transactionStatus(params.get("vnp_TransactionStatus"))
                .transactionNo(params.get("vnp_TransactionNo"))
                .bankCode(params.get("vnp_BankCode"))
                .payDate(params.get("vnp_PayDate"))
                .amount(calculateOrderTotal(order))
                .build();
    }

    private Map<String, String> copySignedFields(Map<String, String> params) {
        Map<String, String> fields = new TreeMap<>();
        params.forEach((key, value) -> {
            if (!SECURE_HASH.equals(key) && !SECURE_HASH_TYPE.equals(key) && StringUtils.hasText(value)) {
                fields.put(key, value);
            }
        });
        return fields;
    }

    private String buildHashData(Map<String, String> params) {
        StringJoiner joiner = new StringJoiner("&");
        params.forEach((key, value) -> {
            if (StringUtils.hasText(value)) {
                joiner.add(urlEncode(key) + "=" + urlEncode(value));
            }
        });
        return joiner.toString();
    }

    private String buildQueryString(Map<String, String> params) {
        StringJoiner joiner = new StringJoiner("&");
        params.forEach((key, value) -> {
            if (StringUtils.hasText(value)) {
                joiner.add(urlEncode(key) + "=" + urlEncode(value));
            }
        });
        return joiner.toString();
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String hmacSha512(String data, String secretKey) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA512");
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac.init(keySpec);
            return HexFormat.of().formatHex(hmac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Could not sign VNPAY request", exception);
        }
    }
}
