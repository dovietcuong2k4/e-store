package com.example.eStore.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_vouchers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserVoucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "voucher_id", nullable = false)
    private Voucher voucher;

    @Column(nullable = false)
    private String status; // AVAILABLE, USED, EXPIRED

    private LocalDateTime assignedAt;

    private LocalDateTime usedAt;

    @PrePersist
    protected void onAssign() {
        if (assignedAt == null) assignedAt = LocalDateTime.now();
        if (status == null) status = "AVAILABLE";
    }
}
