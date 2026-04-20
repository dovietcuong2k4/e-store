package com.example.eStore.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateVoucherRequest {
    @NotBlank
    private String code;
    
    @NotBlank
    private String discountType;
    
    @NotNull
    private Double discountValue;
    
    private Double minOrderValue;
    
    @NotNull
    private LocalDateTime startDate;
    
    @NotNull
    private LocalDateTime endDate;
}
