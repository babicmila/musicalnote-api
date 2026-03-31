package com.musicalnote.musicalnote.service;

import com.musicalnote.musicalnote.dto.AuthDto;
import com.musicalnote.musicalnote.entity.User;
import com.musicalnote.musicalnote.enums.Role;
import com.musicalnote.musicalnote.exception.BusinessException;
import com.musicalnote.musicalnote.repository.UserRepository;
import com.musicalnote.musicalnote.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private AuthDto.RegisterRequest registerRequest;
    private AuthDto.LoginRequest loginRequest;
    private User savedUser;

    @BeforeEach
    void setUp() {
        registerRequest = new AuthDto.RegisterRequest();
        registerRequest.setName("Jane Doe");
        registerRequest.setEmail("jane@email.com");
        registerRequest.setPassword("secret123");

        loginRequest = new AuthDto.LoginRequest();
        loginRequest.setEmail("jane@email.com");
        loginRequest.setPassword("secret123");

        savedUser = User.builder()
                .id(1L)
                .name("Jane Doe")
                .email("jane@email.com")
                .passwordHash("hashedPassword")
                .role(Role.STUDENT)
                .build();
    }

    @Test
    void register_success() {
        when(userRepository.existsByEmail("jane@email.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtUtil.generateToken(any(User.class))).thenReturn("token123");

        AuthDto.AuthResponse response = authService.register(registerRequest);

        assertThat(response.getToken()).isEqualTo("token123");
        assertThat(response.getEmail()).isEqualTo("jane@email.com");
        assertThat(response.getRole()).isEqualTo(Role.STUDENT);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_throwsException_whenEmailAlreadyExists() {
        when(userRepository.existsByEmail("jane@email.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Email already in use");

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_success() {
        when(userRepository.findByEmail("jane@email.com")).thenReturn(Optional.of(savedUser));
        when(jwtUtil.generateToken(savedUser)).thenReturn("token123");

        AuthDto.AuthResponse response = authService.login(loginRequest);

        assertThat(response.getToken()).isEqualTo("token123");
        assertThat(response.getName()).isEqualTo("Jane Doe");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_throwsBadCredentials_whenPasswordWrong() {
        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager).authenticate(any());

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class);
    }
}
