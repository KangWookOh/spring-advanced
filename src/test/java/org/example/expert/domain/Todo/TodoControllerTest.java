package org.example.expert.domain.Todo;

import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.todo.controller.TodoController;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.service.TodoService;
import org.example.expert.domain.user.dto.request.UserRoleChangeRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TodoControllerTest {

    @InjectMocks
    private TodoController todoController;


    @Mock
    private TodoService todoService;

    private AuthUser testUser;
    private UserResponse testUserResponse;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUser = new AuthUser(1L, "test@example.com", UserRole.USER);
        testUserResponse = new UserResponse(1L, "test@example.com");
        testDateTime = LocalDateTime.now();
    }

    @Test
    void 할일_저장_성공() {
        // Given
        TodoSaveRequest request = new TodoSaveRequest("테스트 할일", "테스트 내용");
        TodoSaveResponse expectedResponse = new TodoSaveResponse(1L, "테스트 할일", "테스트 내용", "맑음", testUserResponse);

        when(todoService.saveTodo(testUser, request)).thenReturn(expectedResponse);

        // When
        ResponseEntity<TodoSaveResponse> response = todoController.saveTodo(testUser, request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        assertEquals("맑음", response.getBody().getWeather());
        assertEquals(testUserResponse, response.getBody().getUser());
        verify(todoService).saveTodo(testUser, request);
    }

    @Test
    void 할일_저장_실패_빈_내용() {
        // Given
        TodoSaveRequest request = new TodoSaveRequest("테스트 할일", "");
        when(todoService.saveTodo(testUser, request)).thenThrow(new IllegalArgumentException("내용은 비어있을 수 없습니다."));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> todoController.saveTodo(testUser, request));
        verify(todoService).saveTodo(testUser, request);
    }

    @Test
    void 할일_목록_조회_성공() {
        // Given
        int page = 1;
        int size = 10;
        List<TodoResponse> todoList = List.of(
                new TodoResponse(1L, "할일 1", "내용 1", "맑음", testUserResponse, testDateTime, testDateTime),
                new TodoResponse(2L, "할일 2", "내용 2", "흐림", testUserResponse, testDateTime, testDateTime)
        );
        Page<TodoResponse> expectedPage = new PageImpl<>(todoList);

        when(todoService.getTodos(page, size)).thenReturn(expectedPage);

        // When
        ResponseEntity<Page<TodoResponse>> response = todoController.getTodos(page, size);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedPage, response.getBody());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getContent().size());
        assertEquals("맑음", response.getBody().getContent().get(0).getWeather());
        assertEquals(testUserResponse, response.getBody().getContent().get(0).getUser());
        assertEquals(testDateTime, response.getBody().getContent().get(0).getCreatedAt());
        assertEquals(testDateTime, response.getBody().getContent().get(0).getModifiedAt());
        verify(todoService).getTodos(page, size);
    }

    @Test
    void 할일_목록_조회_잘못된_페이지_번호() {
        // Given
        int invalidPage = 0;
        int size = 10;
        Page<TodoResponse> emptyPage = new PageImpl<>(Collections.emptyList());
        when(todoService.getTodos(invalidPage, size)).thenReturn(emptyPage);

        // When
        ResponseEntity<Page<TodoResponse>> response = todoController.getTodos(invalidPage, size);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getContent().isEmpty());
        assertEquals(0, response.getBody().getTotalElements());
        verify(todoService).getTodos(invalidPage, size);
    }

    @Test
    void 할일_목록_조회_정상_페이지_번호() {
        // Given
        int validPage = 1;
        int size = 10;
        List<TodoResponse> todoList = List.of(
                new TodoResponse(1L, "할일 1", "내용 1", "맑음", testUserResponse, testDateTime, testDateTime),
                new TodoResponse(2L, "할일 2", "내용 2", "흐림", testUserResponse, testDateTime, testDateTime)
        );
        Page<TodoResponse> expectedPage = new PageImpl<>(todoList);
        when(todoService.getTodos(validPage, size)).thenReturn(expectedPage);

        // When
        ResponseEntity<Page<TodoResponse>> response = todoController.getTodos(validPage, size);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getContent().size());
        assertEquals(expectedPage, response.getBody());
        verify(todoService).getTodos(validPage, size);
    }

    @Test
    void 특정_할일_조회_성공() {
        // Given
        long todoId = 1L;
        TodoResponse expectedResponse = new TodoResponse(todoId, "테스트 할일", "테스트 내용", "맑음", testUserResponse, testDateTime, testDateTime);

        when(todoService.getTodo(todoId)).thenReturn(expectedResponse);

        // When
        ResponseEntity<TodoResponse> response = todoController.getTodo(todoId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedResponse, response.getBody());
        assertEquals("맑음", response.getBody().getWeather());
        assertEquals(testUserResponse, response.getBody().getUser());
        assertEquals(testDateTime, response.getBody().getCreatedAt());
        assertEquals(testDateTime, response.getBody().getModifiedAt());
        verify(todoService).getTodo(todoId);
    }

    @Test
    void 특정_할일_조회_실패_존재하지_않는_ID() {
        // Given
        long nonExistentId = 999L;

        when(todoService.getTodo(nonExistentId)).thenThrow(new IllegalArgumentException("해당 ID의 할일이 존재하지 않습니다."));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> todoController.getTodo(nonExistentId));
    }

    @Test
    void 할일_저장_날씨_정보_포함_확인() {
        // Given
        TodoSaveRequest request = new TodoSaveRequest("테스트 할일", "테스트 내용");
        TodoSaveResponse expectedResponse = new TodoSaveResponse(1L, "테스트 할일", "테스트 내용", "비", testUserResponse);

        when(todoService.saveTodo(testUser, request)).thenReturn(expectedResponse);

        // When
        ResponseEntity<TodoSaveResponse> response = todoController.saveTodo(testUser, request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("비", response.getBody().getWeather());
    }

    @Test
    void 할일_저장_제목_최대_길이_초과() {
        // Given
        String longTitle = "a".repeat(101); // 101 characters
        TodoSaveRequest request = new TodoSaveRequest(longTitle, "테스트 내용");
        when(todoService.saveTodo(testUser, request)).thenThrow(new IllegalArgumentException("제목은 100자를 초과할 수 없습니다."));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> todoController.saveTodo(testUser, request));
        verify(todoService).saveTodo(testUser, request);
    }

    @Test
    void 할일_저장_내용_최대_길이_초과() {
        // Given
        String longContent = "a".repeat(1001); // 1001 characters
        TodoSaveRequest request = new TodoSaveRequest("테스트 제목", longContent);
        when(todoService.saveTodo(testUser, request)).thenThrow(new IllegalArgumentException("내용은 1000자를 초과할 수 없습니다."));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> todoController.saveTodo(testUser, request));
        verify(todoService).saveTodo(testUser, request);
    }

    @Test
    void 할일_목록_조회_빈_페이지() {
        // Given
        int page = 1;
        int size = 10;
        Page<TodoResponse> emptyPage = new PageImpl<>(List.of());
        when(todoService.getTodos(page, size)).thenReturn(emptyPage);

        // When
        ResponseEntity<Page<TodoResponse>> response = todoController.getTodos(page, size);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getContent().isEmpty());
        assertEquals(0, response.getBody().getTotalElements());
        verify(todoService).getTodos(page, size);
    }

    @Test
    void 할일_목록_조회_페이지_크기_최대값_초과() {
        // Given
        int page = 1;
        int invalidSize = 101; // Assuming 100 is the max allowed size
        when(todoService.getTodos(page, invalidSize)).thenThrow(new IllegalArgumentException("페이지 크기는 100을 초과할 수 없습니다."));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> todoController.getTodos(page, invalidSize));
        verify(todoService).getTodos(page, invalidSize);
    }

    @Test
    void 할일_저장_날씨_정보_누락() {
        // Given
        TodoSaveRequest request = new TodoSaveRequest("테스트 할일", "테스트 내용");
        TodoSaveResponse responseWithoutWeather = new TodoSaveResponse(1L, "테스트 할일", "테스트 내용", null, testUserResponse);
        when(todoService.saveTodo(testUser, request)).thenReturn(responseWithoutWeather);

        // When
        ResponseEntity<TodoSaveResponse> response = todoController.saveTodo(testUser, request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNull(response.getBody().getWeather());
        verify(todoService).saveTodo(testUser, request);
    }

}