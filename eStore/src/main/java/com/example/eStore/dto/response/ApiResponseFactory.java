package com.example.eStore.dto.response;

import com.example.eStore.dto.BaseResultDTO;
import org.springframework.data.domain.Page;

import java.util.Collection;

public final class ApiResponseFactory {

    private ApiResponseFactory() {
    }

    public static <T> BaseResultDTO<T> success(String message, T data) {
        return BaseResultDTO.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .count(resolveCount(data))
                .build();
    }

    public static BaseResultDTO<Void> success(String message) {
        return BaseResultDTO.<Void>builder()
                .success(true)
                .message(message)
                .build();
    }

    public static BaseResultDTO<Void> error(String message, String errorCode) {
        return BaseResultDTO.<Void>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .build();
    }

    private static Integer resolveCount(Object data) {
        if (data == null) {
            return null;
        }

        if (data instanceof Page<?> page) {
            return page.getNumberOfElements();
        }

        if (data instanceof Collection<?> collection) {
            return collection.size();
        }

        return 1;
    }
}
