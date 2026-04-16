package com.example.eStore.controller;

import com.example.eStore.dto.BaseResultDTO;
import com.example.eStore.dto.response.ApiResponseFactory;
import com.example.eStore.exception.AppException;
import com.example.eStore.exception.ContactMailException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class ApiExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<BaseResultDTO<Void>> handleAppException(AppException exception) {
        HttpStatus status = exception.getErrorCode().contains("NOT_FOUND")
                ? HttpStatus.NOT_FOUND
                : HttpStatus.BAD_REQUEST;

        return ResponseEntity.status(status)
                .body(ApiResponseFactory.error(exception.getMessage(), exception.getErrorCode()));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<BaseResultDTO<Void>> handleNotFound(NoSuchElementException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponseFactory.error("Resource not found", "RESOURCE_NOT_FOUND"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResultDTO<Void>> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining(", "));

        return ResponseEntity.badRequest()
                .body(ApiResponseFactory.error(message, "VALIDATION_ERROR"));
    }

    @ExceptionHandler(ContactMailException.class)
    public ResponseEntity<BaseResultDTO<Void>> handleContactMail(ContactMailException exception) {
        log.error("Contact email sending failed", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseFactory.error("Send contact mail failed", "CONTACT_SEND_MAIL_FAILED"));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<BaseResultDTO<Void>> handleBusiness(RuntimeException exception) {
        return ResponseEntity.badRequest()
                .body(ApiResponseFactory.error(exception.getMessage(), "UNEXPECTED_BUSINESS_ERROR"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResultDTO<Void>> handleUnexpected(Exception exception) {
        log.error("Unhandled exception", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseFactory.error("Internal server error", "INTERNAL_SERVER_ERROR"));
    }

    private String formatFieldError(FieldError fieldError) {
        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
    }
}
