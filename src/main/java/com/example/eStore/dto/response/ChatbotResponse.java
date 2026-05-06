package com.example.eStore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotResponse {
    private String answer;
    private List<ChatbotProductSuggestionResponse> suggestions;
    private boolean aiEnabled;
}
