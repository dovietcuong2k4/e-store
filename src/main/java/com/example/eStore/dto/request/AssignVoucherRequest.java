package com.example.eStore.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignVoucherRequest {
    @NotNull
    private Long voucherId;
    
    @NotNull
    private Long userId;
}
