package com.example.eStore.repository;

import com.example.eStore.entity.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"user", "shipper", "orderItems", "orderItems.product"})
    List<Order> findByUserIdOrderByOrderDateDesc(Long userId);

    @EntityGraph(attributePaths = {"user", "shipper", "orderItems", "orderItems.product"})
    List<Order> findByStatusInOrderByOrderDateDesc(List<String> statuses);

    @EntityGraph(attributePaths = {"user", "shipper", "orderItems", "orderItems.product"})
    List<Order> findByShipperIdAndStatusInOrderByOrderDateDesc(Long shipperId, List<String> statuses);

    @EntityGraph(attributePaths = {"user", "shipper", "orderItems", "orderItems.product"})
    @Query("""
            select o
            from Order o
            where (:status is null or o.status = :status)
              and (:shipperId is null or o.shipper.id = :shipperId)
              and (:fromDate is null or o.orderDate >= :fromDate)
              and (:toDate is null or o.orderDate < :toDate)
            order by o.orderDate desc
            """)
    List<Order> searchForAdmin(String status, Long shipperId, LocalDateTime fromDate, LocalDateTime toDate);
}