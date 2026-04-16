package com.example.eStore.service;

import com.example.eStore.dto.request.ContactRequest;
import com.example.eStore.exception.ContactMailException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncMailService {

    private final JavaMailSender mailSender;

    @Value("${contact.support-email:support@yourshop.com}")
    private String supportEmail;

    @Value("${spring.mail.username:no-reply@yourshop.com}")
    private String fromEmail;

    @Async
    public void sendMailAsync(ContactRequest request, Long contactId) {
        log.info("Starting async email send for contactId={}", contactId);
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(supportEmail);
            helper.setReplyTo(request.getEmail());
            helper.setSubject("[CONTACT] " + request.getSubject());
            helper.setText(buildMailBody(request, contactId), false);

            mailSender.send(mimeMessage);
            log.info("Successfully sent async email for contactId={}", contactId);
        } catch (Exception exception) {
            log.error("Failed to send contact email async. contactId={}, email={}",
                    contactId, request.getEmail(), exception);
            // Since this is async, throwing an exception here won't affect the original HTTP request
            // but is good for logging/monitoring.
        }
    }

    private String buildMailBody(ContactRequest request, Long contactId) {
        return "Contact ID: " + contactId + "\n"
                + "Name: " + request.getName() + "\n"
                + "Email: " + request.getEmail() + "\n"
                + "Phone: " + (request.getPhone() == null ? "" : request.getPhone()) + "\n"
                + "Subject: " + request.getSubject() + "\n"
                + "Message:\n" + request.getMessage();
    }
}
