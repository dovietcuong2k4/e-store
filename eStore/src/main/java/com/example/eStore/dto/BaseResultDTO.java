package com.example.eStore.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseResultDTO<T> {
    private boolean success;
    private String message;
    private T data;
    private String errorCode;
    private Integer count;
}
