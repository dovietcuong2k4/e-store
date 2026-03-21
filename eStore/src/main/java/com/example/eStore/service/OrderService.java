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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private static final Map<String, String> NEXT_ORDER_STATUS = Map.of(
            Constants.OrderStatus.CREATED, Constants.OrderStatus.PENDING,
            Constants.OrderStatus.PENDING, Constants.OrderStatus.SHIPPING,
            Constants.OrderStatus.SHIPPING, Constants.OrderStatus.COMPLETED
    );

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;

    @Transactional
    public BaseResultDTO<Void> createOrder(Long userId, CreateOrderRequest request) {

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

    public BaseResultDTO<Void> updateStatus(Long orderId, String status) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(
                        "Order not found",
                        Constants.ErrorCode.Order.UPDATE_NOT_FOUND
                ));
        validateStatusTransition(order.getStatus(), status);

        order.setStatus(status);

        orderRepository.save(order);
        return ApiResponseFactory.success(resolveUpdateStatusMessage(status));
    }

    private void validateStatusTransition(String currentStatus, String nextStatus) {
        String allowedNextStatus = NEXT_ORDER_STATUS.get(currentStatus);

        if (!nextStatus.equals(allowedNextStatus)) {
            throw new AppException(
                    "Order status can only move from " + currentStatus + " to " + allowedNextStatus,
                    Constants.ErrorCode.Order.UPDATE_INVALID_STATUS
            );
        }
    }

    public BaseResultDTO<List<Order>> getUserOrders(Long userId) {
        return ApiResponseFactory.success(
                Constants.Message.Order.GET_SUCCESS,
                orderRepository.findByUserId(userId)
        );
    }

    private String resolveUpdateStatusMessage(String status) {
        return switch (status) {
            case Constants.OrderStatus.PENDING -> Constants.Message.Order.CONFIRM_SUCCESS;
            case Constants.OrderStatus.SHIPPING -> Constants.Message.Order.SHIPPING_SUCCESS;
            case Constants.OrderStatus.COMPLETED -> Constants.Message.Order.COMPLETE_SUCCESS;
            default -> "Update order status successfully";
        };
    }
}
