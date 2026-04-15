package com.example.eStore.security;

import com.example.eStore.entity.User;
import com.example.eStore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<GrantedAuthority> authorities = new ArrayList<>();
        user.getRoles().forEach(role -> {
            String name = role.getName();
            String authority = name != null && name.startsWith("ROLE_") ? name : "ROLE_" + name;
            authorities.add(new SimpleGrantedAuthority(authority));
        });

        return new AuthenticatedUser(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                authorities
        );
    }
}