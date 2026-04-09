package com.example.eStore.service;

import com.example.eStore.dto.BaseResultDTO;
import com.example.eStore.dto.constants.Constants;
import com.example.eStore.dto.request.CreateOrderRequest;
import com.example.eStore.entity.Cart;
import com.example.eStore.entity.CartItem;
import com.example.eStore.entity.Order;
import com.example.eStore.entity.Product;
import com.example.eStore.entity.User;
import com.example.eStore.exception.AppException;
import com.example.eStore.repository.CartItemRepository;
import com.example.eStore.repository.CartRepository;
import com.example.eStore.repository.OrderItemRepository;
import com.example.eStore.repository.OrderRepository;
import com.example.eStore.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrder_movesCartItemsIntoOrder() {
        Long userId = 2L;
        User user = User.builder().id(userId).email("member@gmail.com").build();
        Cart cart = new Cart(1L, user, 57980000L, null);
        Product macbookAir = Product.builder().id(3L).price(23990000L).build();
        Product macbookAir256 = Product.builder().id(4L).price(28990000L).build();
        List<CartItem> cartItems = List.of(
                new CartItem(10L, cart, macbookAir, 1),
                new CartItem(11L, cart, macbookAir256, 1)
        );
        CreateOrderRequest request = new CreateOrderRequest();
        request.setReceiverName("Nguyen Van A");
        request.setReceiverPhone("0900000001");
        request.setReceiverAddress("Ha Noi");
        request.setNote("Deliver quickly");

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(cart.getId())).thenReturn(cartItems);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        BaseResultDTO<Void> result = orderService.createOrder(userId, request);

        assertTrue(result.isSuccess());
        assertEquals(Constants.Message.Order.CREATE_SUCCESS, result.getMessage());

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();
        assertEquals(Constants.OrderStatus.CREATED, savedOrder.getStatus());
        assertEquals("Nguyen Van A", savedOrder.getReceiverName());
        assertNotNull(savedOrder.getOrderDate());

        verify(orderItemRepository, times(2)).save(any());
        verify(cartItemRepository).deleteAll(cartItems);
        assertEquals(0L, cart.getTotalPrice());
        verify(cartRepository).save(cart);
    }

    @Test
    void createOrder_throwsWhenCartMissing() {
        when(cartRepository.findByUserId(2L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> orderService.createOrder(2L, new CreateOrderRequest()));

        assertEquals(Constants.ErrorCode.Order.CREATE_CART_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void createOrder_throwsWhenCartIsEmpty() {
        Cart cart = new Cart(1L, User.builder().id(2L).build(), 0L, null);

        when(cartRepository.findByUserId(2L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(cart.getId())).thenReturn(List.of());

        AppException exception = assertThrows(AppException.class,
                () -> orderService.createOrder(2L, new CreateOrderRequest()));

        assertEquals(Constants.ErrorCode.Order.CREATE_EMPTY_CART, exception.getErrorCode());
    }

    @Test
    void createOrder_throwsWhenUserMissing() {
        Cart cart = new Cart(1L, User.builder().id(2L).build(), 0L, null);
        Product product = Product.builder().id(3L).price(23990000L).build();
        List<CartItem> cartItems = List.of(new CartItem(10L, cart, product, 1));

        when(cartRepository.findByUserId(2L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(cart.getId())).thenReturn(cartItems);
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> orderService.createOrder(2L, new CreateOrderRequest()));

        assertEquals(Constants.ErrorCode.Order.CREATE_USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void updateStatus_allowsOnlyNextSequentialStatus() {
        Order order = Order.builder()
                .id(31L)
                .status(Constants.OrderStatus.PENDING)
                .build();

        when(orderRepository.findById(31L)).thenReturn(Optional.of(order));

        BaseResultDTO<Void> result = orderService.updateStatus(31L, Constants.OrderStatus.SHIPPING);

        assertEquals(Constants.Message.Order.SHIPPING_SUCCESS, result.getMessage());
        assertEquals(Constants.OrderStatus.SHIPPING, order.getStatus());
        verify(orderRepository).save(order);
    }

    @Test
    void updateStatus_rejectsSkippingAhead() {
        Order order = Order.builder()
                .id(32L)
                .status(Constants.OrderStatus.CREATED)
                .build();

        when(orderRepository.findById(32L)).thenReturn(Optional.of(order));

        AppException exception = assertThrows(
                AppException.class,
                () -> orderService.updateStatus(32L, Constants.OrderStatus.SHIPPING)
        );

        assertEquals("Order status can only move from CREATED to PENDING", exception.getMessage());
        assertEquals(Constants.ErrorCode.Order.UPDATE_INVALID_STATUS, exception.getErrorCode());
    }

    @Test
    void updateStatus_rejectsChangesAfterCompleted() {
        Order order = Order.builder()
                .id(32L)
                .status(Constants.OrderStatus.COMPLETED)
                .build();

        when(orderRepository.findById(32L)).thenReturn(Optional.of(order));

        AppException exception = assertThrows(
                AppException.class,
                () -> orderService.updateStatus(32L, Constants.OrderStatus.COMPLETED)
        );

        assertEquals("Order status can only move from COMPLETED to null", exception.getMessage());
    }

    @Test
    void updateStatus_throwsWhenOrderMissing() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> orderService.updateStatus(999L, Constants.OrderStatus.PENDING));

        assertEquals(Constants.ErrorCode.Order.UPDATE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void getUserOrders_returnsOrdersFromRepository() {
        List<Order> orders = List.of(
                Order.builder().id(31L).status(Constants.OrderStatus.PENDING).build(),
                Order.builder().id(32L).status(Constants.OrderStatus.CREATED).build()
        );
        when(orderRepository.findByUserId(2L)).thenReturn(orders);

        BaseResultDTO<List<Order>> result = orderService.getUserOrders(2L);

        assertTrue(result.isSuccess());
        assertEquals(Constants.Message.Order.GET_SUCCESS, result.getMessage());
        assertEquals(2, result.getCount());
        assertEquals(orders, result.getData());
    }
}
