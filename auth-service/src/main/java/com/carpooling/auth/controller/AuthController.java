package com.carpooling.auth.controller;

import com.carpooling.auth.dto.LoginRequest;
import com.carpooling.auth.dto.LoginResponse;
import com.carpooling.auth.dto.RegisterRequest;
import com.carpooling.auth.dto.UserDTO;
import com.carpooling.auth.service.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        UserDTO updated = authService.updateUser(id, userDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/user/{id}")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id) {
        authService.deactivateUser(id);
        return ResponseEntity.noContent().build();
    }

}
