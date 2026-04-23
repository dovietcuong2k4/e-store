package com.example.eStore.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProfileResponse {
    private UserResponse user;
    private List<ProfileFieldMetadataResponse> fields;
}
