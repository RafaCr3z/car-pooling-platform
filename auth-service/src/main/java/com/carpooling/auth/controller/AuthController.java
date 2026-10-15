package com.carpooling.auth.controller;

import com.carpooling.auth.dto.*;
import com.carpooling.auth.service.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@RequestBody RegisterRequest request) {
        UserDTO userDTO = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(userDTO);
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(authService.getAllUsers());
    }

    @GetMapping("/drivers")
    public ResponseEntity<List<UserDTO>> getAllDrivers() {
        return ResponseEntity.ok(authService.getAllDrivers());
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        UserDTO userDTO = authService.getUserById(id);
        return ResponseEntity.ok(userDTO);
    }

    @GetMapping("/user/email/{email}")
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email) {
        UserDTO userDTO = authService.getUserByEmail(email);
        return ResponseEntity.ok(userDTO);
    }

    @PutMapping("/user/{id}")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable Long id,
            @RequestBody UserDTO userDTO,
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt) {
        if (jwt != null) {
            com.carpooling.common.security.CurrentUser.from(jwt).requireSelfOrAdmin(id);
        }
        UserDTO updated = authService.updateUser(id, userDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/user/{id}")
    public ResponseEntity<Void> deactivateUser(
            @PathVariable Long id,
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt) {
        if (jwt != null) {
            com.carpooling.common.security.CurrentUser.from(jwt).requireSelfOrAdmin(id);
        }
        authService.deactivateUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/user/{id}/rate")
    public ResponseEntity<UserRatingDTO> rateUser(@PathVariable Long id, @RequestBody RateUserRequest request) {
        UserRatingDTO ratingDTO = authService.rateUser(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ratingDTO);
    }

    @GetMapping("/user/{id}/ratings")
    public ResponseEntity<List<UserRatingDTO>> getUserRatings(@PathVariable Long id) {
        return ResponseEntity.ok(authService.getUserRatings(id));
    }

    @PutMapping("/user/{id}/block")
    public ResponseEntity<Void> blockUser(@PathVariable Long id) {
        authService.setBlockedStatus(id, true);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/user/{id}/unblock")
    public ResponseEntity<Void> unblockUser(@PathVariable Long id) {
        authService.setBlockedStatus(id, false);
        return ResponseEntity.noContent().build();
    }

}
