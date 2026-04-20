package com.example.eStore.repository;

import com.example.eStore.entity.UserVoucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserVoucherRepository extends JpaRepository<UserVoucher, Long> {
    List<UserVoucher> findByUserIdAndStatus(Long userId, String status);
    List<UserVoucher> findByUserId(Long userId);
    
    // Find vouchers that should be expired but are still AVAILABLE
    List<UserVoucher> findByStatusAndVoucherEndDateBefore(String status, LocalDateTime date);
}
