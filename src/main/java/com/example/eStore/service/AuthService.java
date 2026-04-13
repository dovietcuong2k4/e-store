package com.example.eStore.service;

import com.example.eStore.dto.AuthResponse;
import com.example.eStore.dto.BaseResultDTO;
import com.example.eStore.dto.LoginRequest;
import com.example.eStore.dto.RegisterRequest;
import com.example.eStore.dto.constants.Constants;
import com.example.eStore.dto.response.ApiResponseFactory;
import com.example.eStore.entity.Role;
import com.example.eStore.entity.User;
import com.example.eStore.exception.AppException;
import com.example.eStore.repository.RoleRepository;
import com.example.eStore.repository.UserRepository;
import com.example.eStore.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public BaseResultDTO<Void> register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(
                    "Email existed",
                    Constants.ErrorCode.User.REGISTER_EMAIL_EXISTS
            );
        }

        User user = new User();

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        Role role =
                roleRepository.findByName("CUSTOMER")
                        .orElseThrow(() -> new AppException(
                                "Customer role not found",
                                Constants.ErrorCode.User.REGISTER_ROLE_NOT_FOUND
                        ));

        user.setRoles(Set.of(role));

        userRepository.save(user);
        return ApiResponseFactory.success(Constants.Message.Auth.REGISTER_SUCCESS);
    }

    public BaseResultDTO<AuthResponse> login(LoginRequest request) {

        User user =
                userRepository.findByEmail(request.getEmail())
                        .orElseThrow(() -> new AppException(
                                "Email not found",
                                Constants.ErrorCode.User.LOGIN_EMAIL_NOT_FOUND
                        ));

        if (!passwordEncoder.matches(request.getPassword(),
                user.getPassword())) {
            throw new AppException(
                    "Invalid password",
                    Constants.ErrorCode.User.LOGIN_INVALID_PASSWORD
            );
        }

        String token = jwtService.generateToken(user.getEmail());

        return ApiResponseFactory.success(
                Constants.Message.Auth.LOGIN_SUCCESS,
                new AuthResponse(token)
        );
    }
}
