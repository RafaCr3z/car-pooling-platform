package com.carpooling.auth.config;

import com.carpooling.auth.entity.User;
import com.carpooling.auth.entity.UserRating;
import com.carpooling.auth.entity.UserRole;
import com.carpooling.auth.repository.UserRatingRepository;
import com.carpooling.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Set;

@Component
@Profile({"!k8s & !prod"})
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserRatingRepository userRatingRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        try {
            initializeDemoUsers();
        } catch (DataIntegrityViolationException e) {
            log.warn("Utilizadores de demonstração já inseridos concorrentemente por outra réplica. Ignorando: {}", e.getMessage());
        }
    }

    private void initializeDemoUsers() {
        if (userRepository.count() > 0) {
            log.info("Base de dados de utilizadores já possui dados. Ignorando inicialização.");
            return;
        }

        log.info("A inicializar utilizadores de demonstração...");

        // 1. Admin
        User admin = User.builder()
                .email("admin@carpooling.pt")
                .password(passwordEncoder.encode("admin123"))
                .fullName("Administrador do Sistema")
                .phoneNumber("910000001")
                .bio("Administrador responsável pela moderação e métricas da plataforma.")
                .roles(Set.of(UserRole.ADMIN))
                .isActive(true)
                .isBlocked(false)
                .rating(5.0)
                .ratingCount(0)
                .build();
        userRepository.save(admin);

        // 2. Condutor João Silva
        User driver1 = User.builder()
                .email("joao.silva@carpooling.pt")
                .password(passwordEncoder.encode("password123"))
                .fullName("João Silva")
                .phoneNumber("912345678")
                .bio("Condutor diário no trajeto Castelo Branco - Lisboa. Condução segura e pontual.")
                .roles(Set.of(UserRole.DRIVER))
                .isActive(true)
                .isBlocked(false)
                .rating(4.9)
                .ratingCount(12)
                .build();
        userRepository.save(driver1);

        // 3. Passageira Maria Santos
        User passenger1 = User.builder()
                .email("maria.santos@carpooling.pt")
                .password(passwordEncoder.encode("password123"))
                .fullName("Maria Santos")
                .phoneNumber("923456789")
                .bio("Estudante no Politécnico de Castelo Branco (EST-IPCB).")
                .roles(Set.of(UserRole.PASSENGER))
                .isActive(true)
                .isBlocked(false)
                .rating(5.0)
                .ratingCount(8)
                .build();
        userRepository.save(passenger1);

        // 4. Condutor Carlos Mendes
        User driver2 = User.builder()
                .email("carlos.mendes@carpooling.pt")
                .password(passwordEncoder.encode("password123"))
                .fullName("Carlos Mendes")
                .phoneNumber("934567890")
                .bio("Viagens regulares Covilhã - Porto ao fim de semana.")
                .roles(Set.of(UserRole.DRIVER))
                .isActive(true)
                .isBlocked(false)
                .rating(4.8)
                .ratingCount(6)
                .build();
        userRepository.save(driver2);

        // Exemplos de avaliações
        userRatingRepository.save(UserRating.builder()
                .raterId(passenger1.getId())
                .raterName("Maria Santos")
                .ratedUserId(driver1.getId())
                .tripId(1L)
                .score(5)
                .comment("Excelente viagem, muito simpático e pontual!")
                .createdAt(LocalDateTime.now().minusDays(2))
                .build());

        log.info("Utilizadores de demonstração criados com sucesso: admin@carpooling.pt, joao.silva@carpooling.pt, maria.santos@carpooling.pt");
    }
}
