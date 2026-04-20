package com.example.eStore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreviewVoucherResponse {
    private boolean eligible;
    private long discountAmount;
    /** Error code from validation when eligible is false (e.g. VOUCHER_EXPIRED). */
    private String reasonCode;
}
