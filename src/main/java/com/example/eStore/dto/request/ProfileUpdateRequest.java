package com.example.eStore.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class ProfileUpdateRequest {

    @NotNull(message = "updates is required")
    private Map<String, String> updates = new HashMap<>();
}
