package com.example.eStore.controller.api;

import com.example.eStore.dto.BaseResultDTO;
import com.example.eStore.dto.request.ContactRequest;
import com.example.eStore.service.MailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
public class ContactController {

    private final MailService mailService;

    @PostMapping
    public ResponseEntity<BaseResultDTO<Void>> sendContact(@Valid @RequestBody ContactRequest request) {
        return ResponseEntity.ok(mailService.sendContact(request));
    }
}
