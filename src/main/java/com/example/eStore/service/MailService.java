package com.example.eStore.service;

import com.example.eStore.dto.BaseResultDTO;
import com.example.eStore.dto.constants.Constants;
import com.example.eStore.dto.request.ContactRequest;
import com.example.eStore.dto.response.ApiResponseFactory;
import com.example.eStore.entity.Contact;
import com.example.eStore.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private final ContactRepository contactRepository;
    private final AsyncMailService asyncMailService;

    public BaseResultDTO<Void> sendContact(ContactRequest request) {
        Contact contact = saveContact(request);
        asyncMailService.sendMailAsync(request, contact.getId());
        return ApiResponseFactory.success(Constants.Message.Contact.SEND_SUCCESS);
    }

    private Contact saveContact(ContactRequest request) {
        Contact contact = new Contact();
        contact.setName(request.getName());
        contact.setEmail(request.getEmail());
        contact.setPhone(request.getPhone());
        contact.setSubject(request.getSubject());
        contact.setMessage(request.getMessage());
        contact.setContactDate(LocalDateTime.now());
        contact.setStatus("NEW");
        return contactRepository.save(contact);
    }
}
