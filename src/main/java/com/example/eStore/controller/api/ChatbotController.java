package com.example.eStore.controller.api;

import com.example.eStore.dto.BaseResultDTO;
import com.example.eStore.dto.request.ChatbotRequest;
import com.example.eStore.dto.response.ApiResponseFactory;
import com.example.eStore.dto.response.ChatbotResponse;
import com.example.eStore.service.ChatbotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {
    private final ChatbotService chatbotService;

    @PostMapping("/message")
    public ResponseEntity<BaseResultDTO<ChatbotResponse>> message(@Valid @RequestBody ChatbotRequest request) {
        return ResponseEntity.ok(ApiResponseFactory.success(
                "Chatbot response generated successfully",
                chatbotService.chat(request)
        ));
    }
}
