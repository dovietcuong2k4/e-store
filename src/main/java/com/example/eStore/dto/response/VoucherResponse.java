package com.example.eStore.dto.response;

import com.example.eStore.entity.Voucher;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class VoucherResponse {
    private Long id;
    private String code;
    private String discountType;
    private Double discountValue;
    private Double minOrderValue;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean active;

    public static VoucherResponse from(Voucher voucher) {
        return VoucherResponse.builder()
                .id(voucher.getId())
                .code(voucher.getCode())
                .discountType(voucher.getDiscountType())
                .discountValue(voucher.getDiscountValue())
                .minOrderValue(voucher.getMinOrderValue())
                .startDate(voucher.getStartDate())
                .endDate(voucher.getEndDate())
                .active(voucher.getActive())
                .build();
    }
}
