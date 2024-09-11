package org.example.expert.domain.auth;

import org.example.expert.domain.auth.controller.AuthController;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.MethodArgumentNotValidException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }



    @Test
    void signup_유효한_요청시_200과_토큰을_반환한다() throws Exception {
        // Given
        SignupRequest signupRequest = new SignupRequest("test@example.com", "password123", "USER");
        SignupResponse signupResponse = new SignupResponse("Bearer_token_123");
        when(authService.signup(any(SignupRequest.class))).thenReturn(signupResponse);

        // When & Then
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\",\"password\":\"password123\",\"userRole\":\"USER\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bearerToken").value("Bearer_token_123"));

        verify(authService, times(1)).signup(any(SignupRequest.class));
    }

    @Test
    void signup_ADMIN_역할로_요청시_200과_토큰을_반환한다() throws Exception {
        // Given
        SignupRequest signupRequest = new SignupRequest("admin@example.com", "adminpass123", "ADMIN");
        SignupResponse signupResponse = new SignupResponse("Bearer_admin_token_123");
        when(authService.signup(any(SignupRequest.class))).thenReturn(signupResponse);

        // When & Then
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@example.com\",\"password\":\"adminpass123\",\"userRole\":\"ADMIN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bearerToken").value("Bearer_admin_token_123"));

        verify(authService, times(1)).signup(any(SignupRequest.class));
    }
    @Test
    void signup_잘못된_이메일로_요청시_400을_반환한다() throws Exception {
        // When & Then
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"invalid-email\",\"password\":\"password123\",\"userRole\":\"USER\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(result -> result.getResolvedException().getClass().equals(MethodArgumentNotValidException.class));

        verify(authService, never()).signup(any(SignupRequest.class));
    }

    @Test
    void signup_빈_비밀번호로_요청시_400을_반환한다() throws Exception {
        // When & Then
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\",\"password\":\"\",\"userRole\":\"USER\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(result -> result.getResolvedException().getClass().equals(MethodArgumentNotValidException.class));

        verify(authService, never()).signup(any(SignupRequest.class));
    }



    @Test
    void signin_유효한_요청시_200과_토큰을_반환한다() throws Exception {
        // Given
        SigninRequest signinRequest = new SigninRequest("test@example.com", "password123");
        SigninResponse signinResponse = new SigninResponse("Bearer_token_123");
        when(authService.signin(any(SigninRequest.class))).thenReturn(signinResponse);

        // When & Then
        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bearerToken").value("Bearer_token_123"));

        verify(authService, times(1)).signin(any(SigninRequest.class));
    }



}
