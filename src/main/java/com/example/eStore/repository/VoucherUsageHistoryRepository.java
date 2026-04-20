package com.example.eStore.repository;

import com.example.eStore.entity.VoucherUsageHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoucherUsageHistoryRepository extends JpaRepository<VoucherUsageHistory, Long> {
    List<VoucherUsageHistory> findByOrderId(Long orderId);
}
