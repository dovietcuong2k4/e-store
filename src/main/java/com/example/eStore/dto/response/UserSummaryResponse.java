package com.example.eStore.dto.response;

import com.example.eStore.entity.User;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class UserSummaryResponse {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private Set<String> roles;

    public static UserSummaryResponse from(User entity) {
        return UserSummaryResponse.builder()
                .id(entity.getId())
                .fullName(entity.getFullName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .address(entity.getAddress())
                .roles(entity.getRoles().stream().map(role -> role.getName()).collect(java.util.stream.Collectors.toSet()))
                .build();
    }
}
