package com.booknest.service;

import com.booknest.config.JwtService;
import com.booknest.dto.ApiDtos.AuthResponse;
import com.booknest.dto.ApiDtos.LoginRequest;
import com.booknest.dto.ApiDtos.RegisterRequest;
import com.booknest.dto.ApiDtos.UserView;
import com.booknest.entity.AppUser;
import com.booknest.entity.Role;
import com.booknest.exception.NotFoundException;
import com.booknest.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtService jwtService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }
        AppUser user = users.save(new AppUser(request.fullName().trim(), email,
                passwordEncoder.encode(request.password()), Role.STUDENT));
        return new AuthResponse(jwtService.generateToken(user), UserView.from(user));
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));
        AppUser user = users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NotFoundException("Account not found."));
        return new AuthResponse(jwtService.generateToken(user), UserView.from(user));
    }
}
