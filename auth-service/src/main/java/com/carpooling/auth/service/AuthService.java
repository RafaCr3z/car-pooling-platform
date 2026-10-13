package com.carpooling.auth.service;

import com.carpooling.auth.dto.*;
import com.carpooling.auth.entity.User;
import com.carpooling.auth.entity.UserRating;
import com.carpooling.auth.entity.UserRole;
import com.carpooling.auth.repository.UserRatingRepository;
import com.carpooling.auth.repository.UserRepository;
import com.carpooling.auth.security.JwtTokenProvider;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final UserRatingRepository userRatingRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilizador não encontrado"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Credenciais inválidas");
        }

        if (Boolean.TRUE.equals(user.getIsBlocked())) {
            throw new RuntimeException("Conta suspensa por moderação. Contacte o suporte.");
        }

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new RuntimeException("Conta desativada");
        }

        String token = jwtTokenProvider.generateToken(user);
        List<String> roleNames = user.getRoles().stream().map(Enum::name).collect(Collectors.toList());
        String primaryRole = roleNames.isEmpty() ? "PASSENGER" : roleNames.get(0);

        return LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(primaryRole)
                .roles(roleNames)
                .build();
    }

    @Transactional
    public UserDTO register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email já registado");
        }

        Set<UserRole> roles = new HashSet<>();
        if (request.getRole() != null) {
            String roleUpper = request.getRole().toUpperCase().trim();
            if (roleUpper.contains("BOTH") || roleUpper.contains("AMBOS")) {
                roles.add(UserRole.PASSENGER);
                roles.add(UserRole.DRIVER);
            } else {
                try {
                    roles.add(UserRole.valueOf(roleUpper));
                } catch (IllegalArgumentException e) {
                    roles.add(UserRole.PASSENGER);
                }
            }
        } else {
            roles.add(UserRole.PASSENGER);
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .roles(roles)
                .rating(5.0)
                .ratingCount(0)
                .isActive(true)
                .isBlocked(false)
                .build();

        user = userRepository.save(user);
        log.info("Novo utilizador registado: {} com papéis: {}", user.getEmail(), roles);

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

    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getAllDrivers() {
        return userRepository.findByRolesContaining(UserRole.DRIVER).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilizador não encontrado"));

        if (userDTO.getFullName() != null) user.setFullName(userDTO.getFullName());
        if (userDTO.getPhoneNumber() != null) user.setPhoneNumber(userDTO.getPhoneNumber());
        if (userDTO.getBio() != null) user.setBio(userDTO.getBio());

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

    @Transactional
    public void setBlockedStatus(Long id, boolean blocked) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilizador não encontrado"));
        user.setIsBlocked(blocked);
        userRepository.save(user);
        log.info("Utilizador {} bloqueio alterado para: {}", user.getEmail(), blocked);
    }

    @Transactional
    public UserRatingDTO rateUser(Long targetUserId, RateUserRequest request) {
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Utilizador a avaliar não encontrado"));

        User rater = userRepository.findById(request.getRaterId())
                .orElseThrow(() -> new RuntimeException("Avaliador não encontrado"));

        int score = Math.max(1, Math.min(5, request.getScore() != null ? request.getScore() : 5));

        UserRating rating = UserRating.builder()
                .raterId(rater.getId())
                .raterName(rater.getFullName())
                .ratedUserId(targetUser.getId())
                .tripId(request.getTripId())
                .score(score)
                .comment(request.getComment())
                .build();

        rating = userRatingRepository.save(rating);

        // Recalcular rating médio do utilizador
        List<UserRating> allRatings = userRatingRepository.findByRatedUserIdOrderByCreatedAtDesc(targetUserId);
        double avg = allRatings.stream().mapToInt(UserRating::getScore).average().orElse(5.0);
        targetUser.setRating(Math.round(avg * 10.0) / 10.0);
        targetUser.setRatingCount(allRatings.size());
        userRepository.save(targetUser);

        log.info("Avaliação de {} estrelas registada para o utilizador: {}", score, targetUser.getEmail());

        return UserRatingDTO.builder()
                .id(rating.getId())
                .raterId(rating.getRaterId())
                .raterName(rating.getRaterName())
                .ratedUserId(rating.getRatedUserId())
                .tripId(rating.getTripId())
                .score(rating.getScore())
                .comment(rating.getComment())
                .createdAt(rating.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<UserRatingDTO> getUserRatings(Long userId) {
        return userRatingRepository.findByRatedUserIdOrderByCreatedAtDesc(userId).stream()
                .map(r -> UserRatingDTO.builder()
                        .id(r.getId())
                        .raterId(r.getRaterId())
                        .raterName(r.getRaterName())
                        .ratedUserId(r.getRatedUserId())
                        .tripId(r.getTripId())
                        .score(r.getScore())
                        .comment(r.getComment())
                        .createdAt(r.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    private UserDTO mapToDTO(User user) {
        List<String> roleNames = user.getRoles() != null
                ? user.getRoles().stream().map(Enum::name).collect(Collectors.toList())
                : List.of("PASSENGER");
        String primaryRole = roleNames.isEmpty() ? "PASSENGER" : roleNames.get(0);

        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .bio(user.getBio())
                .role(primaryRole)
                .roles(roleNames)
                .rating(user.getRating() != null ? user.getRating() : 5.0)
                .ratingCount(user.getRatingCount() != null ? user.getRatingCount() : 0)
                .isActive(user.getIsActive())
                .isBlocked(user.getIsBlocked())
                .build();
    }

}
