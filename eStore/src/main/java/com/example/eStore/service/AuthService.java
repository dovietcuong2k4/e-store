package com.example.eStore.service;

import com.example.eStore.dto.AuthResponse;
import com.example.eStore.dto.LoginRequest;
import com.example.eStore.dto.RegisterRequest;
import com.example.eStore.entity.Role;
import com.example.eStore.entity.User;
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

    public void register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail()))
            throw new RuntimeException("Email existed");

        User user = new User();

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        Role role =
                roleRepository.findByName("CUSTOMER").orElseThrow();

        user.setRoles(Set.of(role));

        userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {

        User user =
                userRepository.findByEmail(request.getEmail())
                        .orElseThrow();

        if (!passwordEncoder.matches(request.getPassword(),
                user.getPassword()))
            throw new RuntimeException("Invalid password");

        String token = jwtService.generateToken(user.getEmail());

        return new AuthResponse(token);
    }
}
