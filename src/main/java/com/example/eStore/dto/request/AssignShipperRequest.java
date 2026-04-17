package com.example.eStore.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignShipperRequest {
    @NotNull(message = "shipperId is required")
    private Long shipperId;
}
