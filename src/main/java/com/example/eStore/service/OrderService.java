package com.example.eStore.service;

import com.example.eStore.dto.BaseResultDTO;
import com.example.eStore.dto.constants.Constants;
import com.example.eStore.dto.request.CreateOrderRequest;
import com.example.eStore.dto.response.ApiResponseFactory;
import com.example.eStore.entity.Cart;
import com.example.eStore.entity.CartItem;
import com.example.eStore.entity.Order;
import com.example.eStore.entity.OrderItem;
import com.example.eStore.exception.AppException;
import com.example.eStore.repository.*;
import com.example.eStore.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrderService {
    private static final Map<String, Map<String, Set<String>>> ORDER_STATUS_RULES = Map.of(
            Constants.OrderStatus.CREATED, Map.of(
                    Constants.OrderStatus.CONFIRMED, Set.of(Constants.Role.ADMIN, Constants.Role.STAFF),
                    Constants.OrderStatus.CANCELLED, Set.of(Constants.Role.ADMIN, Constants.Role.STAFF)
            ),
            Constants.OrderStatus.CONFIRMED, Map.of(
                    Constants.OrderStatus.PREPARING, Set.of(Constants.Role.STAFF),
                    Constants.OrderStatus.CANCELLED, Set.of(Constants.Role.ADMIN)
            ),
            Constants.OrderStatus.PREPARING, Map.of(
                    Constants.OrderStatus.READY_FOR_SHIPPING, Set.of(Constants.Role.STAFF),
                    Constants.OrderStatus.CANCELLED, Set.of(Constants.Role.ADMIN)
            ),
            Constants.OrderStatus.READY_FOR_SHIPPING, Map.of(
                    Constants.OrderStatus.SHIPPING, Set.of(Constants.Role.SHIPPER)
            ),
            Constants.OrderStatus.SHIPPING, Map.of(
                    Constants.OrderStatus.DELIVERED, Set.of(Constants.Role.SHIPPER),
                    Constants.OrderStatus.DELIVERY_FAILED, Set.of(Constants.Role.SHIPPER)
            ),
            Constants.OrderStatus.DELIVERY_FAILED, Map.of(
                    Constants.OrderStatus.READY_FOR_SHIPPING, Set.of(Constants.Role.STAFF)
            )
    );

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;

    @Transactional
    public BaseResultDTO<Void> createOrder(CreateOrderRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(
                        "Cart not found",
                        Constants.ErrorCode.Order.CREATE_CART_NOT_FOUND
                ));

        List<CartItem> cartItems =
                cartItemRepository.findByCartId(cart.getId());

        if (cartItems.isEmpty()) {
            throw new AppException(
                    "Cart is empty",
                    Constants.ErrorCode.Order.CREATE_EMPTY_CART
            );
        }

        Order order = new Order();
        order.setUser(userRepository.findById(userId)
                .orElseThrow(() -> new AppException(
                        "User not found",
                        Constants.ErrorCode.Order.CREATE_USER_NOT_FOUND
                )));
        order.setReceiverName(request.getReceiverName());
        order.setReceiverPhone(request.getReceiverPhone());
        order.setReceiverAddress(request.getReceiverAddress());
        order.setNote(request.getNote());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(Constants.OrderStatus.CREATED);

        orderRepository.save(order);

        for (CartItem item : cartItems) {

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(item.getProduct());
            orderItem.setQuantity(item.getQuantity());

            orderItem.setPrice(item.getProduct().getPrice());

            orderItemRepository.save(orderItem);
        }

        cartItemRepository.deleteAll(cartItems);
        cart.setTotalPrice(0L);
        cartRepository.save(cart);
        return ApiResponseFactory.success(Constants.Message.Order.CREATE_SUCCESS);
    }

    public BaseResultDTO<Void> updateStatus(Long orderId, String status, String actorRole) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(
                        "Order not found",
                        Constants.ErrorCode.Order.UPDATE_NOT_FOUND
                ));
        validateStatusTransition(order.getStatus(), status, actorRole);

        order.setStatus(status);
        if (Constants.OrderStatus.SHIPPING.equals(status)) {
            order.setShippingDate(LocalDateTime.now());
        }
        if (Constants.OrderStatus.DELIVERED.equals(status)) {
            order.setReceivedDate(LocalDateTime.now());
        }

        orderRepository.save(order);
        return ApiResponseFactory.success(resolveUpdateStatusMessage(status));
    }

    private void validateStatusTransition(
            String currentStatus,
            String nextStatus,
            String actorRole) {

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
    }

    public BaseResultDTO<List<Order>> getUserOrders() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponseFactory.success(
                Constants.Message.Order.GET_SUCCESS,
                orderRepository.findByUserId(userId)
        );
    }

    private String resolveUpdateStatusMessage(String status) {
        return switch (status) {
            case Constants.OrderStatus.CONFIRMED -> Constants.Message.Order.CONFIRM_SUCCESS;
            case Constants.OrderStatus.PREPARING -> Constants.Message.Order.PREPARING_SUCCESS;
            case Constants.OrderStatus.READY_FOR_SHIPPING -> Constants.Message.Order.READY_SUCCESS;
            case Constants.OrderStatus.SHIPPING -> Constants.Message.Order.SHIPPING_SUCCESS;
            case Constants.OrderStatus.DELIVERED -> Constants.Message.Order.DELIVERY_SUCCESS;
            case Constants.OrderStatus.DELIVERY_FAILED -> Constants.Message.Order.DELIVERY_FAILED;
            case Constants.OrderStatus.CANCELLED -> Constants.Message.Order.CANCEL_SUCCESS;
            default -> "Update order status successfully";
        };
    }
}
