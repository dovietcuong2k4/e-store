package com.example.eStore.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PreviewVoucherRequest {

    @NotNull
    private Long userVoucherId;

    @NotNull
    private Long orderTotal;
}
