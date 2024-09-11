package org.example.expert.domain.user.controller;

import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserController userController;



    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void 사용자_정보_조회_테스트() throws Exception {
        // Given
        long userId = 1L;
        UserResponse userResponse = new UserResponse(userId, "test@example.com");
        given(userService.getUser(userId)).willReturn(userResponse);

        // When
        ResultActions result = mockMvc.perform(get("/users/{userId}", userId));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void 비밀번호_변경_성공_테스트() throws Exception {
        // Given
        long userId = 1L;
        UserChangePasswordRequest request = new UserChangePasswordRequest("oldPassword", "newPassword123A");
        AuthUser authUser = new AuthUser(userId, "test@example.com", UserRole.USER);

        doNothing().when(userService).changePassword(anyLong(), any(UserChangePasswordRequest.class));

        // When
        userController.changePassword(authUser, request);

        // Then
        verify(userService).changePassword(userId, request);
    }


    @Test
    void 비밀번호_변경_실패_올드패스워드_누락_테스트() throws Exception {
        // Given
        long userId = 1L;
        UserChangePasswordRequest request = new UserChangePasswordRequest("", "newPassword");
        AuthUser authUser = new AuthUser(userId, "test@example.com", UserRole.USER);

        // When
        ResultActions result = mockMvc.perform(put("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .requestAttr("authUser", authUser));

        // Then
        result.andExpect(status().isBadRequest());
    }

    @Test
    void 비밀번호_변경_실패_뉴패스워드_누락_테스트() throws Exception {
        // Given
        long userId = 1L;
        UserChangePasswordRequest request = new UserChangePasswordRequest("oldPassword", "");
        AuthUser authUser = new AuthUser(userId, "test@example.com", UserRole.USER);

        // When
        ResultActions result = mockMvc.perform(put("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .requestAttr("authUser", authUser));

        // Then
        result.andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "USER"})
    void UserRole_of_성공_테스트(String role) {
        // When
        UserRole userRole = UserRole.of(role);

        // Then
        assertEquals(role.toUpperCase(), userRole.name());
    }

    @Test
    void UserRole_of_실패_테스트() {
        // When & Then
        assertThrows(InvalidRequestException.class, () -> UserRole.of("INVALID_ROLE"));
    }





}