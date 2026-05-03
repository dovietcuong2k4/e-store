package com.example.eStore.service;

import com.example.eStore.dto.BaseResultDTO;
import com.example.eStore.dto.constants.Constants;
import com.example.eStore.dto.request.ContactRequest;
import com.example.eStore.dto.response.ApiResponseFactory;
import com.example.eStore.dto.response.ContactResponse;
import com.example.eStore.entity.Contact;
import com.example.eStore.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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

    public BaseResultDTO<List<ContactResponse>> getAllContacts() {
        List<Contact> contacts = contactRepository.findAll();
        List<ContactResponse> responses = contacts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ApiResponseFactory.success(Constants.Message.Contact.GET_ALL_CONTACTS_SUCCESS ,responses);
    }

    private ContactResponse mapToResponse(Contact contact) {
        return ContactResponse.builder()
                .id(contact.getId())
                .name(contact.getName())
                .email(contact.getEmail())
                .phone(contact.getPhone())
                .subject(contact.getSubject())
                .message(contact.getMessage())
                .replyMessage(contact.getReplyMessage())
                .contactDate(contact.getContactDate())
                .replyDate(contact.getReplyDate())
                .status(contact.getStatus())
                .responderName(contact.getResponder() != null ? contact.getResponder().getFullName() : null)
                .build();
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
