package com.example.eStore.entity;

import com.example.eStore.profile.ProfileField;
import com.example.eStore.profile.ProfileFieldType;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ProfileField(label = "Mã người dùng", type = ProfileFieldType.NUMBER, order = 0, required = true)
    private Long id;

    @ProfileField(
        label = "Họ và tên",
        type = ProfileFieldType.TEXT,
        order = 1,
        required = true,
        maxLength = 120,
        placeholder = "Nhập họ và tên"
    )
    private String fullName;

    @ProfileField(
        label = "Email",
        type = ProfileFieldType.EMAIL,
        order = 2,
        required = true,
        maxLength = 255,
        placeholder = "you@example.com"
    )
    private String email;

    private String password;

    @ProfileField(
        label = "Số điện thoại",
        type = ProfileFieldType.TEL,
        order = 3,
        maxLength = 20,
        pattern = "^(?:0|\\+84)\\d{9,10}$",
        placeholder = "0901234567"
    )
    private String phone;

    @ProfileField(
        label = "Địa chỉ",
        type = ProfileFieldType.TEXTAREA,
        order = 4,
        maxLength = 255,
        placeholder = "Nhập địa chỉ nhận hàng"
    )
    private String address;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @ProfileField(label = "Vai trò", type = ProfileFieldType.TAGS, order = 5)
    private Set<Role> roles = new HashSet<>();
}