package com.example.eStore.service;

import com.example.eStore.dto.BaseResultDTO;
import com.example.eStore.dto.constants.Constants;
import com.example.eStore.dto.request.AssignShipperRequest;
import com.example.eStore.dto.request.CartItemRequest;
import com.example.eStore.dto.request.CreateOrderRequest;
import com.example.eStore.dto.response.ApiResponseFactory;
import com.example.eStore.dto.response.CreateOrderResponse;
import com.example.eStore.dto.response.OrderWorkflowResponse;
import com.example.eStore.dto.response.UserSummaryResponse;
import com.example.eStore.entity.*;
import com.example.eStore.exception.AppException;
import com.example.eStore.repository.*;
import com.example.eStore.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
@Service
@RequiredArgsConstructor
public class OrderService {
    private static final Map<String, Map<String, Set<String>>> ORDER_STATUS_RULES = Map.of(
            Constants.OrderStatus.CREATED, Map.of(
                    Constants.OrderStatus.PROCESSING, Set.of(Constants.Role.ADMIN, Constants.Role.STAFF),
                    Constants.OrderStatus.CANCELLED, Set.of(Constants.Role.ADMIN, Constants.Role.STAFF, Constants.Role.CUSTOMER)
            ),
            Constants.OrderStatus.PROCESSING, Map.of(
                    Constants.OrderStatus.READY_FOR_SHIPPING, Set.of(Constants.Role.ADMIN, Constants.Role.STAFF),
                    Constants.OrderStatus.CANCELLED, Set.of(Constants.Role.ADMIN, Constants.Role.STAFF)
            ),
            Constants.OrderStatus.READY_FOR_SHIPPING, Map.of(
                    Constants.OrderStatus.SHIPPING, Set.of(Constants.Role.SHIPPER)
            ),
            Constants.OrderStatus.SHIPPING, Map.of(
                    Constants.OrderStatus.DELIVERED, Set.of(Constants.Role.SHIPPER),
                    Constants.OrderStatus.DELIVERY_FAILED, Set.of(Constants.Role.SHIPPER)
            ),
            Constants.OrderStatus.DELIVERY_FAILED, Map.of(
                    Constants.OrderStatus.READY_FOR_SHIPPING, Set.of(Constants.Role.ADMIN, Constants.Role.STAFF)
            )
    );

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderHistoryRepository orderHistoryRepository;
    private final VoucherService voucherService;
    private final UserVoucherRepository userVoucherRepository;

    @Transactional
    public BaseResultDTO<CreateOrderResponse> createOrder(CreateOrderRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();

        Order order = new Order();
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(
                        "User not found",
                        Constants.ErrorCode.Order.CREATE_USER_NOT_FOUND
                ));
        order.setUser(currentUser);
        
        order.setReceiverName(request.getReceiverName());
        order.setReceiverPhone(request.getReceiverPhone());
        order.setReceiverAddress(request.getReceiverAddress());
        order.setNote(request.getNote());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(Constants.OrderStatus.CREATED);

        orderRepository.save(order);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(
                        "Cart not found",
                        Constants.ErrorCode.Order.CREATE_CART_NOT_FOUND
                ));

        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());

        if (cartItems.isEmpty()) {
            throw new AppException(
                    "Cart is empty",
                    Constants.ErrorCode.Order.CREATE_EMPTY_CART
            );
        }

        long originalTotalPrice = 0;
        for (CartItem item : cartItems) {
            Product product = productRepository.findByIdWithLock(item.getProduct().getId())
                    .orElseThrow(() -> new AppException(
                            "Product not found",
                            Constants.ErrorCode.Product.UPDATE_NOT_FOUND
                    ));

            if (product.getStockQuantity() < item.getQuantity()) {
                throw new AppException(
                        "Product " + product.getName() + " is out of stock",
                        Constants.ErrorCode.Order.CREATE_OUT_OF_STOCK
                );
            }

            product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
            product.setSoldQuantity((product.getSoldQuantity() == null ? 0 : product.getSoldQuantity()) + item.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(item.getQuantity());
            orderItem.setPrice(product.getPrice());
            orderItemRepository.save(orderItem);
            
            originalTotalPrice += product.getPrice() * item.getQuantity();
        }

        // Apply voucher if present: compute discount first; mark USED only after order is saved
        if (request.getUserVoucherId() != null) {
            long discount = voucherService.validateAndComputeDiscount(
                    request.getUserVoucherId(),
                    originalTotalPrice,
                    userId,
                    true
            );
            order.setDiscountAmount(discount);
            order.setUserVoucher(userVoucherRepository.getReferenceById(request.getUserVoucherId()));
        } else {
            order.setDiscountAmount(0L);
        }

        orderRepository.save(order);

        if (request.getUserVoucherId() != null) {
            voucherService.finalizeVoucherUsage(request.getUserVoucherId(), order.getId(), userId);
        }

        cartItemRepository.deleteAll(cartItems);
        cart.setTotalPrice(0L);
        cartRepository.save(cart);

        // Record initial history
        orderHistoryRepository.save(OrderHistory.builder()
                .order(order)
                .fromStatus(null)
                .toStatus(Constants.OrderStatus.CREATED)
                .changedBy(Constants.Role.CUSTOMER + " (" + userId + ")")
                .changedAt(LocalDateTime.now())
                .build());

        return ApiResponseFactory.success(
            Constants.Message.Order.CREATE_SUCCESS,
            new CreateOrderResponse(order.getId())
        );
    }

    @Transactional
    public BaseResultDTO<Void> updateStatus(Long orderId, String status, String actorRole) {

        Order order = findOrderOrThrow(orderId);
        String oldStatus = order.getStatus();

        validateStatusTransition(order, status, actorRole);
        validateShipperOwnership(order, actorRole);

        order.setStatus(status);
        if (Constants.OrderStatus.SHIPPING.equals(status)) {
            order.setShippingDate(LocalDateTime.now());
        }
        if (Constants.OrderStatus.DELIVERED.equals(status)) {
            order.setReceivedDate(LocalDateTime.now());
        }

        orderRepository.save(order);

        // Record history
        orderHistoryRepository.save(OrderHistory.builder()
                .order(order)
                .fromStatus(oldStatus)
                .toStatus(status)
                .changedBy(actorRole + " (" + SecurityUtils.getCurrentUserId() + ")")
                .changedAt(LocalDateTime.now())
                .build());

        // Log voucher cancellation if applicable
        if (Constants.OrderStatus.CANCELLED.equals(status) && order.getUserVoucher() != null) {
            voucherService.logVoucherUsage(
                    order.getUserVoucher().getVoucher().getId(),
                    order.getUser().getId(),
                    order.getId(),
                    Constants.VoucherAction.CANCELLED,
                    "Order #" + orderId + " cancelled. Voucher remains USED."
            );
        }

        return ApiResponseFactory.success(resolveUpdateStatusMessage(status));
    }

    @Transactional
    public BaseResultDTO<Void> assignShipper(Long orderId, AssignShipperRequest request) {
        if (!SecurityUtils.hasRole(Constants.Role.STAFF) && !SecurityUtils.hasRole(Constants.Role.ADMIN)) {
            throw new AppException(
                    "Current user is not allowed to perform this action",
                    Constants.ErrorCode.Order.UPDATE_INVALID_STATUS
            );
        }

        Order order = findOrderOrThrow(orderId);
        if (!Constants.OrderStatus.READY_FOR_SHIPPING.equals(order.getStatus()) &&
            !Constants.OrderStatus.DELIVERY_FAILED.equals(order.getStatus())) {
            throw new AppException(
                    "Order can only be assigned when it is READY_FOR_SHIPPING or DELIVERY_FAILED",
                    Constants.ErrorCode.Order.UPDATE_INVALID_STATUS
            );
        }

        User shipper = userRepository.findById(request.getShipperId())
                .orElseThrow(() -> new AppException(
                        "Shipper not found",
                        Constants.ErrorCode.Order.ASSIGN_SHIPPER_INVALID
                ));

        boolean isShipper = shipper.getRoles()
                .stream()
                .anyMatch(role -> Constants.Role.SHIPPER.equals(role.getName()));
        if (!isShipper) {
            throw new AppException(
                    "Assigned user is not a shipper",
                    Constants.ErrorCode.Order.ASSIGN_SHIPPER_INVALID
            );
        }

        order.setShipper(shipper);
        if (Constants.OrderStatus.DELIVERY_FAILED.equals(order.getStatus())) {
            order.setStatus(Constants.OrderStatus.READY_FOR_SHIPPING);
        }
        orderRepository.save(order);
        return ApiResponseFactory.success(Constants.Message.Order.ASSIGN_SHIPPER_SUCCESS);
    }

    private void validateStatusTransition(
            Order order,
            String nextStatus,
            String actorRole) {

        ensureCurrentUserHasRole(actorRole);

        String currentStatus = order.getStatus();
        Map<String, Set<String>> nextStatusMap = ORDER_STATUS_RULES.get(currentStatus);

        if (nextStatusMap == null || !nextStatusMap.containsKey(nextStatus)) {
            throw new AppException(
                    "Order status cannot move from " + currentStatus + " to " + nextStatus,
                    Constants.ErrorCode.Order.UPDATE_INVALID_STATUS
            );
        }

        Set<String> allowedRoles = nextStatusMap.get(nextStatus);
        if (!allowedRoles.contains(actorRole)) {
            throw new AppException(
                    "Role " + actorRole + " is not allowed to move from " + currentStatus + " to " + nextStatus,
                    Constants.ErrorCode.Order.UPDATE_INVALID_STATUS
            );
        }

        // Customer specific validation
        if (Constants.Role.CUSTOMER.equals(actorRole)) {
            Long currentUserId = SecurityUtils.getCurrentUserId();
            if (!currentUserId.equals(order.getUser().getId())) {
                throw new AppException(
                        "You can only cancel your own orders",
                        Constants.ErrorCode.Order.CANCEL_NOT_ALLOWED
                );
            }
        }
    }

    public BaseResultDTO<List<OrderWorkflowResponse>> getUserOrders() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponseFactory.success(
                Constants.Message.Order.GET_SUCCESS,
                orderRepository.findByUserIdOrderByOrderDateDesc(userId)
                        .stream()
                        .map(OrderWorkflowResponse::from)
                        .toList()
        );
    }

    public BaseResultDTO<List<OrderWorkflowResponse>> getStaffOrders() {
        ensureCurrentUserHasRole(Constants.Role.STAFF);
        return ApiResponseFactory.success(
                Constants.Message.Order.GET_SUCCESS,
                orderRepository.findAllByOrderByOrderDateDesc()
                        .stream()
                        .map(OrderWorkflowResponse::from)
                        .toList()
        );
    }

    public BaseResultDTO<List<OrderWorkflowResponse>> getShipperOrders() {
        ensureCurrentUserHasRole(Constants.Role.SHIPPER);
        Long shipperId = SecurityUtils.getCurrentUserId();
        return ApiResponseFactory.success(
                Constants.Message.Order.GET_SUCCESS,
                orderRepository.findByShipperIdAndStatusInOrderByOrderDateDesc(
                                shipperId,
                                List.of(Constants.OrderStatus.READY_FOR_SHIPPING, Constants.OrderStatus.SHIPPING)
                        ).stream()
                        .map(OrderWorkflowResponse::from)
                        .toList()
        );
    }

    public BaseResultDTO<List<OrderWorkflowResponse>> getAdminOrders(String status, Long shipperId, LocalDate date) {
        ensureCurrentUserHasRole(Constants.Role.ADMIN);
        LocalDateTime fromDate = date != null ? date.atStartOfDay() : null;
        LocalDateTime toDate = date != null ? date.plusDays(1).atStartOfDay() : null;
        return ApiResponseFactory.success(
                Constants.Message.Order.GET_SUCCESS,
                orderRepository.searchForAdmin(status, shipperId, fromDate, toDate)
                        .stream()
                        .map(OrderWorkflowResponse::from)
                        .toList()
        );
    }

    public BaseResultDTO<List<UserSummaryResponse>> getAssignableShippers() {
        ensureCurrentUserHasRole(Constants.Role.STAFF);
        return ApiResponseFactory.success(
                Constants.Message.Order.GET_SUCCESS,
                userRepository.findAll()
                        .stream()
                        .filter(user -> user.getRoles().stream().anyMatch(role -> Constants.Role.SHIPPER.equals(role.getName())))
                        .map(UserSummaryResponse::from)
                        .toList()
        );
    }

    private Order findOrderOrThrow(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(
                        "Order not found",
                        Constants.ErrorCode.Order.UPDATE_NOT_FOUND
                ));
    }

    private void ensureCurrentUserHasRole(String role) {
        if (!SecurityUtils.hasRole(role)) {
            throw new AppException(
                    "Current user is not allowed to perform this action",
                    Constants.ErrorCode.Order.UPDATE_INVALID_STATUS
            );
        }
    }

    private void validateShipperOwnership(Order order, String actorRole) {
        if (!Constants.Role.SHIPPER.equals(actorRole)) {
            return;
        }

        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (order.getShipper() == null || !currentUserId.equals(order.getShipper().getId())) {
            throw new AppException(
                    "This order is not assigned to the current shipper",
                    Constants.ErrorCode.Order.UPDATE_INVALID_STATUS
            );
        }
    }

    private String resolveUpdateStatusMessage(String status) {
        return switch (status) {
            case Constants.OrderStatus.PROCESSING -> Constants.Message.Order.PROCESSING_SUCCESS;
            case Constants.OrderStatus.READY_FOR_SHIPPING -> Constants.Message.Order.READY_SUCCESS;
            case Constants.OrderStatus.SHIPPING -> Constants.Message.Order.SHIPPING_SUCCESS;
            case Constants.OrderStatus.DELIVERED -> Constants.Message.Order.DELIVERY_SUCCESS;
            case Constants.OrderStatus.DELIVERY_FAILED -> Constants.Message.Order.DELIVERY_FAILED;
            case Constants.OrderStatus.CANCELLED -> Constants.Message.Order.CANCEL_SUCCESS;
            default -> "Update order status successfully";
        };
    }
}
