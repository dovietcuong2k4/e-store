package com.example.eStore.controller;

import com.example.eStore.dto.BaseResultDTO;
import com.example.eStore.dto.response.ApiResponseFactory;
import com.example.eStore.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    private final UserRepository userRepository;

    public TestController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @RequestMapping("/users")
    public ResponseEntity<BaseResultDTO<Long>> getUsers() {
        return ResponseEntity.ok(
                ApiResponseFactory.success("Get total users successfully", userRepository.count())
        );
    }
}
