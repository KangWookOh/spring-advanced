package org.example.expert.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.domain.user.dto.request.UserRoleChangeRequest;
import org.example.expert.domain.user.service.UserAdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserAdminController.class)
class UserAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserAdminService userAdminService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserRoleChangeRequest userRoleChangeRequest;

    @BeforeEach
    void setUp() {
        userRoleChangeRequest = new UserRoleChangeRequest("ADMIN");
    }

    @Test
    void changeUserRole_성공() throws Exception {
        long userId = 1L;

        doNothing().when(userAdminService).changeUserRole(eq(userId), argThat(request ->
                request.getRole().equals(userRoleChangeRequest.getRole())
        ));

        mockMvc.perform(patch("/admin/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRoleChangeRequest)))
                .andExpect(status().isOk());

        verify(userAdminService, times(1)).changeUserRole(eq(userId), argThat(request ->
                request.getRole().equals(userRoleChangeRequest.getRole())
        ));
    }

    @Test
    void changeUserRole_유효하지않은요청_처리() throws Exception {
        long userId = 1L;
        UserRoleChangeRequest invalidRequest = new UserRoleChangeRequest("INVALID_ROLE");

        mockMvc.perform(patch("/admin/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isOk());  // 현재 구현에서는 200 OK를 반환합니다.

        // 유효하지 않은 요청이더라도 서비스 메소드가 호출되는지 확인합니다.
        verify(userAdminService, times(1)).changeUserRole(eq(userId), argThat(request ->
                request.getRole().equals("INVALID_ROLE")
        ));
    }
}