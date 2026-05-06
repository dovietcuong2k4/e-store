package com.example.eStore.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotRequest {
    @NotBlank(message = "Message is required")
    @Size(max = 1200, message = "Message must be at most 1200 characters")
    private String message;

    @Size(max = 12, message = "History must contain at most 12 messages")
    private List<ChatbotHistoryMessage> history;
}
