package com.example.eStore.service;

import com.example.eStore.dto.BaseResultDTO;
import com.example.eStore.dto.constants.Constants;
import com.example.eStore.dto.request.AdminUserUpsertRequest;
import com.example.eStore.dto.response.ApiResponseFactory;
import com.example.eStore.dto.response.UserResponse;
import com.example.eStore.entity.Role;
import com.example.eStore.entity.User;
import com.example.eStore.exception.AppException;
import com.example.eStore.repository.RoleRepository;
import com.example.eStore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserManagementService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public BaseResultDTO<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userRepository.findAll()
                .stream()
                .map(this::toUserResponse)
                .toList();

        return ApiResponseFactory.success(Constants.Message.User.ADMIN_GET_ALL_SUCCESS, users);
    }

    public BaseResultDTO<UserResponse> getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException("User not found", Constants.ErrorCode.User.ADMIN_GET_NOT_FOUND));

        return ApiResponseFactory.success(Constants.Message.User.ADMIN_GET_DETAIL_SUCCESS, toUserResponse(user));
    }

    @Transactional
    public BaseResultDTO<UserResponse> createUser(AdminUserUpsertRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException("Email existed", Constants.ErrorCode.User.ADMIN_CREATE_EMAIL_EXISTS);
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setRoles(resolveRoles(request.getRoles(), Constants.ErrorCode.User.ADMIN_CREATE_ROLE_NOT_FOUND));

        User savedUser = userRepository.save(user);
        return ApiResponseFactory.success(Constants.Message.User.ADMIN_CREATE_SUCCESS, toUserResponse(savedUser));
    }

    @Transactional
    public BaseResultDTO<UserResponse> updateUser(Long id, AdminUserUpsertRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException("User not found", Constants.ErrorCode.User.ADMIN_GET_NOT_FOUND));

        if (!user.getEmail().equalsIgnoreCase(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new AppException("Email existed", Constants.ErrorCode.User.ADMIN_UPDATE_EMAIL_EXISTS);
        }

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        user.setRoles(resolveRoles(request.getRoles(), Constants.ErrorCode.User.ADMIN_UPDATE_ROLE_NOT_FOUND));

        User updatedUser = userRepository.save(user);
        return ApiResponseFactory.success(Constants.Message.User.ADMIN_UPDATE_SUCCESS, toUserResponse(updatedUser));
    }

    @Transactional
    public BaseResultDTO<Void> deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException("User not found", Constants.ErrorCode.User.ADMIN_DELETE_NOT_FOUND));

        userRepository.delete(user);
        return ApiResponseFactory.success(Constants.Message.User.ADMIN_DELETE_SUCCESS);
    }

    private Set<Role> resolveRoles(Set<String> roleNames, String errorCode) {
        if (roleNames == null || roleNames.isEmpty()) {
            roleNames = Set.of(Constants.Role.CUSTOMER);
        }

        return roleNames.stream()
                .map(String::trim)
                .map(String::toUpperCase)
                .map(name -> roleRepository.findByName(name)
                        .orElseThrow(() -> new AppException("Role not found: " + name, errorCode)))
                .collect(Collectors.toSet());
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(user.getAddress())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .build();
    }
}
