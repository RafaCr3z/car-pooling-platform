package com.carpooling.auth;

import com.carpooling.auth.dto.LoginRequest;
import com.carpooling.auth.dto.LoginResponse;
import com.carpooling.auth.dto.RateUserRequest;
import com.carpooling.auth.dto.RegisterRequest;
import com.carpooling.auth.dto.UserDTO;
import com.carpooling.auth.dto.UserRatingDTO;
import com.carpooling.auth.entity.User;
import com.carpooling.auth.entity.UserRating;
import com.carpooling.auth.entity.UserRole;
import com.carpooling.auth.repository.UserRatingRepository;
import com.carpooling.auth.repository.UserRepository;
import com.carpooling.auth.security.JwtTokenProvider;
import com.carpooling.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRatingRepository userRatingRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .email("joao.silva@carpooling.pt")
                .password("encoded_pass")
                .fullName("João Silva")
                .phoneNumber("912345678")
                .roles(Set.of(UserRole.DRIVER))
                .rating(4.8)
                .ratingCount(10)
                .isActive(true)
                .isBlocked(false)
                .build();
    }

    @Test
    @DisplayName("Deve autenticar com sucesso quando as credenciais são válidas")
    void testLogin_Success() {
        LoginRequest req = new LoginRequest("joao.silva@carpooling.pt", "password123");

        when(userRepository.findByEmail(req.getEmail())).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("password123", "encoded_pass")).thenReturn(true);
        when(jwtTokenProvider.generateToken(sampleUser)).thenReturn("mocked_jwt_token");

        LoginResponse response = authService.login(req);

        assertNotNull(response);
        assertEquals("mocked_jwt_token", response.getToken());
        assertEquals("joao.silva@carpooling.pt", response.getEmail());
        assertEquals(1L, response.getUserId());
        assertEquals("DRIVER", response.getRole());
    }

    @Test
    @DisplayName("Deve lançar exceção quando a palavra-passe é incorreta")
    void testLogin_InvalidPassword_ThrowsException() {
        LoginRequest req = new LoginRequest("joao.silva@carpooling.pt", "wrong_pass");

        when(userRepository.findByEmail(req.getEmail())).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("wrong_pass", "encoded_pass")).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(req));
        assertTrue(ex.getMessage().contains("Credenciais inválidas"));
    }

    @Test
    @DisplayName("Deve rejeitar autenticação de utilizador com conta suspensa/bloqueada")
    void testLogin_BlockedUser_ThrowsException() {
        sampleUser.setIsBlocked(true);
        LoginRequest req = new LoginRequest("joao.silva@carpooling.pt", "password123");

        when(userRepository.findByEmail(req.getEmail())).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("password123", "encoded_pass")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(req));
        assertTrue(ex.getMessage().contains("Conta suspensa por moderação"));
    }

    @Test
    @DisplayName("Deve lançar exceção ao registar utilizador com email já existente")
    void testRegister_DuplicateEmail_ThrowsException() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("joao.silva@carpooling.pt");
        req.setPassword("password123");

        when(userRepository.existsByEmail("joao.silva@carpooling.pt")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.register(req));
        assertTrue(ex.getMessage().contains("Email já registado"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Deve calcular a média ponderada de avaliação após nova review")
    void testRateUser_Success_RecalculatesAverage() {
        User target = User.builder().id(2L).email("maria@carpooling.pt").rating(5.0).ratingCount(1).build();
        User rater = User.builder().id(1L).fullName("João Silva").build();

        RateUserRequest rateReq = new RateUserRequest();
        rateReq.setRaterId(1L);
        rateReq.setScore(4);
        rateReq.setComment("Excelente pontualidade!");
        rateReq.setTripId(10L);

        when(userRepository.findById(2L)).thenReturn(Optional.of(target));
        when(userRepository.findById(1L)).thenReturn(Optional.of(rater));
        when(userRatingRepository.save(any(UserRating.class))).thenAnswer(i -> {
            UserRating r = i.getArgument(0);
            r.setId(99L);
            return r;
        });

        UserRating r1 = UserRating.builder().score(5).build();
        UserRating r2 = UserRating.builder().score(4).build();
        when(userRatingRepository.findByRatedUserIdOrderByCreatedAtDesc(2L)).thenReturn(List.of(r1, r2));

        UserRatingDTO result = authService.rateUser(2L, rateReq);

        assertNotNull(result);
        assertEquals(4, result.getScore());
        assertEquals("Excelente pontualidade!", result.getComment());
        assertEquals(4.5, target.getRating());
        assertEquals(2, target.getRatingCount());
        verify(userRepository).save(target);
    }

    @Test
    @DisplayName("Deve registar com sucesso um passageiro comum")
    void testRegister_Success_Passenger() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("novo.passageiro@carpooling.pt");
        req.setPassword("segredo123");
        req.setFullName("Novo Passageiro");
        req.setPhoneNumber("933333333");
        req.setRole("PASSENGER");

        when(userRepository.existsByEmail(req.getEmail())).thenReturn(false);
        when(passwordEncoder.encode("segredo123")).thenReturn("hashed_pass");
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(10L);
            return u;
        });

        UserDTO created = authService.register(req);

        assertNotNull(created);
        assertEquals(10L, created.getId());
        assertEquals("novo.passageiro@carpooling.pt", created.getEmail());
        assertEquals("PASSENGER", created.getRole());
        assertTrue(created.getIsActive());
        assertFalse(created.getIsBlocked());
    }

    @Test
    @DisplayName("Deve registar utilizador com múltiplos papéis quando selecionado AMBOS")
    void testRegister_Success_BothRoles() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("hibrido@carpooling.pt");
        req.setPassword("pass123");
        req.setFullName("Condutor e Passageiro");
        req.setRole("AMBOS");

        when(userRepository.existsByEmail(req.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashed_pass");
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(11L);
            return u;
        });

        UserDTO created = authService.register(req);

        assertNotNull(created);
        assertEquals(2, created.getRoles().size());
        assertTrue(created.getRoles().contains("DRIVER"));
        assertTrue(created.getRoles().contains("PASSENGER"));
    }

    @Test
    @DisplayName("Deve obter perfil de utilizador por ID")
    void testGetUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        UserDTO dto = authService.getUserById(1L);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("joao.silva@carpooling.pt", dto.getEmail());
    }

    @Test
    @DisplayName("Deve desativar conta de utilizador")
    void testDeactivateUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        authService.deactivateUser(1L);

        assertFalse(sampleUser.getIsActive());
        verify(userRepository).save(sampleUser);
    }

    @Test
    @DisplayName("Deve alterar estado de bloqueio de utilizador")
    void testSetBlockedStatus() {
        sampleUser.setIsBlocked(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        authService.setBlockedStatus(1L, false);

        assertFalse(sampleUser.getIsBlocked());
        verify(userRepository).save(sampleUser);
    }
}

