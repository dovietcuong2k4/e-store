package com.example.eStore.dto.request;

import lombok.Data;

import java.util.Set;

@Data
public class AdminUserUpsertRequest {
    private String fullName;
    private String email;
    private String password;
    private String phone;
    private String address;
    private Set<String> roles;
}
