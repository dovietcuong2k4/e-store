package com.example.eStore.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "contacts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Contact {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String phone;
    private String subject;
    private String message;
    private String replyMessage;
    private LocalDateTime contactDate;
    private LocalDateTime replyDate;
    private String status;

    @ManyToOne
    @JoinColumn(name = "responder_id")
    private User responder;
}