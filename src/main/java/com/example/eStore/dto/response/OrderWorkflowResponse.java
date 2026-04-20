package com.example.eStore.dto.response;

import com.example.eStore.entity.Order;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderWorkflowResponse {
    private Long id;
    private UserSummaryResponse user;
    private UserSummaryResponse shipper;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String note;
    private LocalDateTime orderDate;
    private LocalDateTime shippingDate;
    private LocalDateTime receivedDate;
    private String status;
    private Long totalPrice;
    private Long discountAmount;
    private List<OrderItemResponse> orderItems;

    public static OrderWorkflowResponse from(Order entity) {
        List<OrderItemResponse> items = entity.getOrderItems() == null
                ? List.of()
                : entity.getOrderItems().stream().map(OrderItemResponse::from).toList();
        long originalPrice = items.stream()
                .mapToLong(item -> item.getPrice() * item.getQuantity())
                .sum();
        
        long discount = entity.getDiscountAmount() != null ? entity.getDiscountAmount() : 0L;

        return OrderWorkflowResponse.builder()
                .id(entity.getId())
                .user(entity.getUser() != null ? UserSummaryResponse.from(entity.getUser()) : null)
                .shipper(entity.getShipper() != null ? UserSummaryResponse.from(entity.getShipper()) : null)
                .receiverName(entity.getReceiverName())
                .receiverPhone(entity.getReceiverPhone())
                .receiverAddress(entity.getReceiverAddress())
                .note(entity.getNote())
                .orderDate(entity.getOrderDate())
                .shippingDate(entity.getShippingDate())
                .receivedDate(entity.getReceivedDate())
                .status(entity.getStatus())
                .totalPrice(originalPrice - discount)
                .discountAmount(discount)
                .orderItems(items)
                .build();
    }
}
