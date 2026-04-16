package com.example.eStore.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ContactRequest {
    @NotBlank(message = "name must not be blank")
    private String name;

    @NotBlank(message = "email must not be blank")
    @Email(message = "email must be a valid email address")
    private String email;

    // Optional by requirement
    private String phone;

    @NotBlank(message = "subject must not be blank")
    private String subject;

    @NotBlank(message = "message must not be blank")
    private String message;
}
