package org.example.expert.domain.auth;

import org.example.expert.domain.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.example.expert.config.JwtUtil;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void 회원가입_성공_테스트() {
        // Given
        SignupRequest request = new SignupRequest("test@example.com", "password", "USER");
        User savedUser = new User("test@example.com", "encodedPassword", UserRole.USER);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtUtil.createToken(any(), anyString(), any())).thenReturn("token");

        // When
        SignupResponse response = authService.signup(request);

        // Then
        assertNotNull(response);
        assertEquals("token", response.getBearerToken());
        verify(userRepository).save(any(User.class));
        verify(jwtUtil).createToken(any(), anyString(), any());
    }

    @Test
    void 회원가입_이메일_중복_테스트() {
        // Given
        SignupRequest request = new SignupRequest("existing@example.com", "password", "USER");
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // When & Then
        assertThrows(InvalidRequestException.class, () -> authService.signup(request));
    }

    @Test
    void 로그인_성공_테스트() {
        // Given
        SigninRequest request = new SigninRequest("test@example.com", "password");
        User user = new User("test@example.com", "encodedPassword", UserRole.USER);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.createToken(any(), anyString(), any())).thenReturn("token");

        // When
        SigninResponse response = authService.signin(request);

        // Then
        assertNotNull(response);
        assertEquals("token", response.getBearerToken());
        verify(jwtUtil).createToken(any(), anyString(), any());
    }

    @Test
    void 로그인_존재하지_않는_사용자_테스트() {
        // Given
        SigninRequest request = new SigninRequest("nonexistent@example.com", "password");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(InvalidRequestException.class, () -> authService.signin(request));
    }

    @Test
    void 로그인_잘못된_비밀번호_테스트() {
        // Given
        SigninRequest request = new SigninRequest("test@example.com", "wrongpassword");
        User user = new User("test@example.com", "encodedPassword", UserRole.USER);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // When & Then
        assertThrows(AuthException.class, () -> authService.signin(request));
    }
    @Test
    void 회원가입_이메일이_널일_경우_테스트() {
        // Given
        SignupRequest request = new SignupRequest(null, "password", "USER");

        // When & Then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            authService.signup(request);
        });

        assertEquals("이미 존재하는 이메일입니다.", exception.getMessage());
        verify(userRepository, never()).existsByEmail(anyString()); // 이메일 중복 체크 메소드가 호출되지 않았는지 확인
    }
}