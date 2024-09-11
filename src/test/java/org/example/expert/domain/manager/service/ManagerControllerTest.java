package org.example.expert.domain.manager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.config.AuthUserArgumentResolver;
import org.example.expert.config.JwtUtil;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.manager.controller.ManagerController;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerResponse;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.List;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ManagerController.class)
@Import({JwtUtil.class, AuthUserArgumentResolver.class})
class ManagerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ManagerService managerService;

    @Autowired
    private JwtUtil jwtUtil;

    private ObjectMapper objectMapper;


    private AuthUser authUser;

    private String token;

    @BeforeEach
    void setUp() {
        // 테스트용 AuthUser 객체 생성 (UserRole.USER 역할)
        authUser = new AuthUser(1L, "test@example.com", UserRole.ADMIN);

        // JWT 토큰 생성, 유저 ID, 이메일, 역할을 포함하여 생성
        token = jwtUtil.createToken(authUser.getId(), authUser.getEmail(), authUser.getUserRole());

        objectMapper = new ObjectMapper();  // ObjectMapper를 초기화

    }

    @Test
    void saveManager_성공() throws Exception {
        // Arrange
        ManagerSaveRequest request = new ManagerSaveRequest(2L);
        UserResponse userResponse = new UserResponse(2L, "manager@example.com");
        ManagerSaveResponse response = new ManagerSaveResponse(1L, userResponse);
        // ArgumentMatcher를 사용하여 AuthUser와 ManagerSaveRequest의 필드 값을 매칭
        when(managerService.saveManager(argThat(new AuthUserMatcher()), eq(1L), argThat(new ManagerSaveRequestMatcher())))
                .thenReturn(response);
        // Act & Assert
        MvcResult result = mockMvc.perform(post("/todos/1/managers")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .requestAttr("userId", 1L)
                        .requestAttr("email", "test@example.com")
                        .requestAttr("role", UserRole.ADMIN.name()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        // 응답 본문 출력
        String responseBody = result.getResponse().getContentAsString();
        System.out.println("Response Body: " + responseBody);

        // 예상 JSON 직렬화
        String expectedJson = objectMapper.writeValueAsString(response);
        System.out.println("Expected JSON: " + expectedJson);

        // Verify
        verify(managerService).saveManager(argThat(new AuthUserMatcher()), eq(1L), argThat(new ManagerSaveRequestMatcher()));
    }

    @Test
    void getManagers_성공() throws Exception {
        List<ManagerResponse> managerResponses = Arrays.asList(
                new ManagerResponse(1L, new UserResponse(2L, "manager1@example.com")),
                new ManagerResponse(2L, new UserResponse(3L, "manager2@example.com"))
        );

        when(managerService.getManagers(1L)).thenReturn(managerResponses);

        mockMvc.perform(get("/todos/1/managers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].user.id").value(2L))
                .andExpect(jsonPath("$[0].user.email").value("manager1@example.com"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].user.id").value(3L))
                .andExpect(jsonPath("$[1].user.email").value("manager2@example.com"));

        verify(managerService).getManagers(1L);
    }

    @Test
    void deleteManager_성공() throws Exception {
        doNothing().when(managerService).deleteManager(anyString(), eq(1L), eq(1L));

        mockMvc.perform(delete("/1/managers/1")
                        .header("Authorization", "Bearer validToken"))
                .andExpect(status().isOk());

        verify(managerService).deleteManager(anyString(), eq(1L), eq(1L));
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public JwtUtil jwtUtil() {
            return new JwtUtil();
        }

    }

    // AuthUserMatcher 클래스 정의
    class AuthUserMatcher implements ArgumentMatcher<AuthUser> {
        @Override
        public boolean matches(AuthUser authUser) {
            return authUser.getId().equals(1L) &&
                    authUser.getEmail().equals("test@example.com") &&
                    authUser.getUserRole().equals(UserRole.ADMIN);
        }
    }
    // ManagerSaveRequestMatcher 클래스 정의
    class ManagerSaveRequestMatcher implements ArgumentMatcher<ManagerSaveRequest> {
        @Override
        public boolean matches(ManagerSaveRequest request) {
            return request.getManagerUserId().equals(2L);
        }
    }
}