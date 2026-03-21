package com.example.eStore.service;

import com.example.eStore.dto.request.CreateOrderRequest;
import com.example.eStore.entity.Cart;
import com.example.eStore.entity.CartItem;
import com.example.eStore.entity.Order;
import com.example.eStore.entity.OrderItem;
import com.example.eStore.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createOrder(Long userId, CreateOrderRequest request) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        List<CartItem> cartItems =
                cartItemRepository.findByCartId(cart.getId());

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        Order order = new Order();
        order.setUser(userRepository.findById(userId).orElseThrow());
        order.setReceiverName(request.getReceiverName());
        order.setReceiverPhone(request.getReceiverPhone());
        order.setReceiverAddress(request.getReceiverAddress());
        order.setNote(request.getNote());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("CREATED");

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
    }

    public void updateStatus(Long orderId, String status) {

        Order order = orderRepository.findById(orderId).orElseThrow();

        order.setStatus(status);

        orderRepository.save(order);
    }

    public List<Order> getUserOrders(Long userId) {
        return orderRepository.findByUserId(userId);
    }
}
