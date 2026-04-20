package com.example.eStore.service;

import com.example.eStore.dto.BaseResultDTO;
import com.example.eStore.dto.constants.Constants;
import com.example.eStore.dto.request.AssignVoucherRequest;
import com.example.eStore.dto.request.CreateVoucherRequest;
import com.example.eStore.dto.request.PreviewVoucherRequest;
import com.example.eStore.dto.response.ApiResponseFactory;
import com.example.eStore.dto.response.PreviewVoucherResponse;
import com.example.eStore.dto.response.UserVoucherResponse;
import com.example.eStore.dto.response.VoucherResponse;
import com.example.eStore.entity.User;
import com.example.eStore.entity.UserVoucher;
import com.example.eStore.entity.Voucher;
import com.example.eStore.entity.VoucherUsageHistory;
import com.example.eStore.exception.AppException;
import com.example.eStore.repository.UserVoucherRepository;
import com.example.eStore.repository.UserRepository;
import com.example.eStore.repository.VoucherRepository;
import com.example.eStore.repository.VoucherUsageHistoryRepository;
import com.example.eStore.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VoucherService {

    private final VoucherRepository voucherRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final UserRepository userRepository;
    private final VoucherUsageHistoryRepository voucherUsageHistoryRepository;

    @Transactional(readOnly = true)
    public BaseResultDTO<List<VoucherResponse>> getAllVoucherTemplates() {
        return ApiResponseFactory.success(
                Constants.Message.Voucher.GET_SUCCESS,
                voucherRepository.findAll().stream().map(VoucherResponse::from).toList()
        );
    }

    @Transactional
    public BaseResultDTO<VoucherResponse> createVoucherTemplate(CreateVoucherRequest request) {
        if (voucherRepository.findByCode(request.getCode()).isPresent()) {
            throw new AppException(
                    "Voucher code already exists",
                    Constants.ErrorCode.Voucher.CODE_EXISTS
            );
        }

        Voucher voucher = Voucher.builder()
                .code(request.getCode())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .minOrderValue(request.getMinOrderValue())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .active(true)
                .build();

        voucher = voucherRepository.save(voucher);
        return ApiResponseFactory.success(
                Constants.Message.Voucher.CREATE_SUCCESS,
                VoucherResponse.from(voucher)
        );
    }

    @Transactional
    public BaseResultDTO<Void> assignVoucherToUser(AssignVoucherRequest request) {
        Voucher voucher = voucherRepository.findById(request.getVoucherId())
                .orElseThrow(() -> new AppException(
                        "Voucher template not found",
                        Constants.ErrorCode.Voucher.TEMPLATE_NOT_FOUND
                ));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(
                        "User not found",
                        Constants.ErrorCode.Voucher.USER_NOT_FOUND
                ));

        UserVoucher uv = UserVoucher.builder()
                .user(user)
                .voucher(voucher)
                .status(Constants.VoucherStatus.AVAILABLE)
                .assignedAt(LocalDateTime.now())
                .build();

        userVoucherRepository.save(uv);
        return ApiResponseFactory.success(Constants.Message.Voucher.ASSIGN_SUCCESS);
    }

    /**
     * Lists all vouchers assigned to the current user (any status).
     * Marks AVAILABLE instances as EXPIRED when the template end date has passed.
     */
    @Transactional
    public BaseResultDTO<List<UserVoucherResponse>> getMyVouchers() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<UserVoucher> list = userVoucherRepository.findByUserId(userId);
        LocalDateTime now = LocalDateTime.now();

        for (UserVoucher uv : list) {
            if (!Constants.VoucherStatus.AVAILABLE.equals(uv.getStatus())) {
                continue;
            }
            Voucher template = uv.getVoucher();
            if (template != null && now.isAfter(template.getEndDate())) {
                uv.setStatus(Constants.VoucherStatus.EXPIRED);
                userVoucherRepository.save(uv);
            }
        }

        return ApiResponseFactory.success(
                Constants.Message.Voucher.GET_SUCCESS,
                list.stream().map(UserVoucherResponse::from).toList()
        );
    }

    /**
     * Read-only preview for checkout UI: does not mark USED and does not persist EXPIRED transitions.
     */
    @Transactional(readOnly = true)
    public BaseResultDTO<PreviewVoucherResponse> previewVoucher(PreviewVoucherRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        try {
            long discount = validateAndComputeDiscount(
                    request.getUserVoucherId(),
                    request.getOrderTotal(),
                    userId,
                    false
            );
            return ApiResponseFactory.success(
                    Constants.Message.Voucher.APPLY_SUCCESS,
                    PreviewVoucherResponse.builder()
                            .eligible(true)
                            .discountAmount(discount)
                            .build()
            );
        } catch (AppException e) {
            return ApiResponseFactory.success(
                    Constants.Message.Voucher.GET_SUCCESS,
                    PreviewVoucherResponse.builder()
                            .eligible(false)
                            .discountAmount(0L)
                            .reasonCode(e.getErrorCode())
                            .build()
            );
        }
    }

    /**
     * Validates rules and returns discount amount. Does not set USED (see {@link #finalizeVoucherUsage}).
     *
     * @param persistExpiredTransition when true, AVAILABLE user vouchers past template end date are saved as EXPIRED
     */
    @Transactional
    public long validateAndComputeDiscount(Long userVoucherId, long orderValue, Long userId,
                                           boolean persistExpiredTransition) {
        UserVoucher uv = userVoucherRepository.findById(userVoucherId)
                .orElseThrow(() -> new AppException(
                        "Voucher not found",
                        Constants.ErrorCode.Voucher.USER_VOUCHER_NOT_FOUND
                ));

        if (!uv.getUser().getId().equals(userId)) {
            throw new AppException(
                    "Voucher does not belong to you",
                    Constants.ErrorCode.Voucher.NOT_OWNED
            );
        }

        if (!Constants.VoucherStatus.AVAILABLE.equals(uv.getStatus())) {
            throw new AppException(
                    "Voucher is not available",
                    Constants.ErrorCode.Voucher.NOT_AVAILABLE
            );
        }

        Voucher template = uv.getVoucher();
        if (!Boolean.TRUE.equals(template.getActive())) {
            throw new AppException(
                    "Voucher is inactive",
                    Constants.ErrorCode.Voucher.INACTIVE
            );
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(template.getStartDate())) {
            throw new AppException(
                    "Voucher is not yet active",
                    Constants.ErrorCode.Voucher.START_DATE_NOT_REACHED
            );
        }

        if (now.isAfter(template.getEndDate())) {
            if (persistExpiredTransition) {
                uv.setStatus(Constants.VoucherStatus.EXPIRED);
                userVoucherRepository.save(uv);
            }
            throw new AppException(
                    "Voucher has expired",
                    Constants.ErrorCode.Voucher.EXPIRED
            );
        }

        double minOrder = template.getMinOrderValue() != null ? template.getMinOrderValue() : 0.0;
        if (orderValue < minOrder) {
            throw new AppException(
                    "Minimum order value not met: " + minOrder,
                    Constants.ErrorCode.Voucher.MIN_VALUE_NOT_MET
            );
        }

        double discount;
        if (Constants.DiscountType.FIXED.equals(template.getDiscountType())) {
            discount = template.getDiscountValue();
        } else if (Constants.DiscountType.PERCENTAGE.equals(template.getDiscountType())) {
            discount = (orderValue * template.getDiscountValue()) / 100.0;
        } else {
            throw new AppException(
                    "Invalid voucher discount type",
                    Constants.ErrorCode.Voucher.TEMPLATE_NOT_FOUND
            );
        }

        long discountAmount = (long) discount;
        if (discountAmount > orderValue) {
            discountAmount = orderValue;
        }
        return discountAmount;
    }

    /**
     * Marks the user voucher as USED and writes USED audit log. Call only after the order row is persisted.
     */
    @Transactional
    public void finalizeVoucherUsage(Long userVoucherId, Long orderId, Long userId) {
        UserVoucher uv = userVoucherRepository.findById(userVoucherId)
                .orElseThrow(() -> new AppException(
                        "Voucher not found",
                        Constants.ErrorCode.Voucher.USER_VOUCHER_NOT_FOUND
                ));

        if (!uv.getUser().getId().equals(userId)) {
            throw new AppException(
                    "Voucher does not belong to you",
                    Constants.ErrorCode.Voucher.NOT_OWNED
            );
        }

        if (!Constants.VoucherStatus.AVAILABLE.equals(uv.getStatus())) {
            throw new AppException(
                    "Voucher is not available",
                    Constants.ErrorCode.Voucher.NOT_AVAILABLE
            );
        }

        LocalDateTime now = LocalDateTime.now();
        uv.setStatus(Constants.VoucherStatus.USED);
        uv.setUsedAt(now);
        userVoucherRepository.save(uv);

        logVoucherUsage(
                uv.getVoucher().getId(),
                userId,
                orderId,
                Constants.VoucherAction.USED,
                "Voucher applied to order #" + orderId
        );
    }

    @Transactional
    public void logVoucherUsage(Long voucherId, Long userId, Long orderId, String action, String note) {
        VoucherUsageHistory history = VoucherUsageHistory.builder()
                .voucherId(voucherId)
                .userId(userId)
                .orderId(orderId)
                .action(action)
                .note(note)
                .build();
        voucherUsageHistoryRepository.save(history);
    }
}
