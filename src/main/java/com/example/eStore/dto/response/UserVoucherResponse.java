package com.example.eStore.dto.response;

import com.example.eStore.entity.UserVoucher;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserVoucherResponse {
    private Long id;
    private VoucherResponse voucher;
    private String status;
    private LocalDateTime assignedAt;
    private LocalDateTime usedAt;

    public static UserVoucherResponse from(UserVoucher uv) {
        return UserVoucherResponse.builder()
                .id(uv.getId())
                .voucher(VoucherResponse.from(uv.getVoucher()))
                .status(uv.getStatus())
                .assignedAt(uv.getAssignedAt())
                .usedAt(uv.getUsedAt())
                .build();
    }
}
