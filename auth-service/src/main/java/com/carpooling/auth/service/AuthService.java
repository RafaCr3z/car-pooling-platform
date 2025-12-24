package com.carpooling.auth.service;

import com.carpooling.auth.dto.LoginRequest;
import com.carpooling.auth.dto.LoginResponse;
import com.carpooling.auth.dto.RegisterRequest;
import com.carpooling.auth.dto.UserDTO;
import com.carpooling.auth.entity.User;
import com.carpooling.auth.entity.UserRole;
import com.carpooling.auth.repository.UserRepository;
import com.carpooling.auth.security.JwtTokenProvider;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilizador não encontrado"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Credenciais inválidas");
        }

        if (!user.getIsActive()) {
            throw new RuntimeException("Conta desativada");
        }

        String token = jwtTokenProvider.generateToken(user.getEmail());

        return new LoginResponse(token, user.getEmail(), user.getFullName());
    }

    @Transactional
    public UserDTO register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email já registado");
        }

        UserRole role;
        try {
            role = UserRole.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            role = UserRole.PASSENGER;
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .role(role)
                .roles(Collections.singleton(role))
                .rating(5.0)
                .isActive(true)
                .build();

        user = userRepository.save(user);

        return mapToDTO(user);
    }

    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilizador não encontrado"));
        return mapToDTO(user);
    }

    @Transactional(readOnly = true)
    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilizador não encontrado"));
        return mapToDTO(user);
    }

    @Transactional
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilizador não encontrado"));

        user.setFullName(userDTO.getFullName());
        user.setPhoneNumber(userDTO.getPhoneNumber());

        user = userRepository.save(user);
        return mapToDTO(user);
    }

    @Transactional
    public void deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilizador não encontrado"));
        user.setIsActive(false);
        userRepository.save(user);
    }

    private UserDTO mapToDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhoneNumber(),
                user.getRole().name(),
                user.getRating(),
                user.getIsActive()
        );
    }

}
