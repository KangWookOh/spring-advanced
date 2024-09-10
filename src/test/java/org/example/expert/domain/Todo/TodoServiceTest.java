package org.example.expert.domain.Todo;

import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.entity.Timestamped;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.todo.service.TodoService;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

    @InjectMocks
    private TodoService todoService;

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private WeatherClient weatherClient;

    @Test
    void 할일저장_성공() {
        // Given
        AuthUser authUser = new AuthUser(1L, "test@example.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        TodoSaveRequest request = new TodoSaveRequest("제목", "내용");
        String weather = "맑음";
        Todo savedTodo = new Todo("제목", "내용", weather, user);

        // Timestamped 필드 설정
        LocalDateTime now = LocalDateTime.now();
        setTimestampedFields(savedTodo, now, now);

        when(weatherClient.getTodayWeather()).thenReturn(weather);
        when(todoRepository.save(any(Todo.class))).thenReturn(savedTodo);

        // When
        TodoSaveResponse response = todoService.saveTodo(authUser, request);

        // Then
        assertNotNull(response);
        assertEquals("제목", response.getTitle());
        assertEquals("내용", response.getContents());
        assertEquals("맑음", response.getWeather());
        assertEquals(1L, response.getUser().getId());
        assertEquals("test@example.com", response.getUser().getEmail());
        assertEquals(1, savedTodo.getManagers().size());
        assertNotNull(savedTodo.getCreatedAt());
        assertNotNull(savedTodo.getModifiedAt());

        verify(weatherClient).getTodayWeather();
        verify(todoRepository).save(any(Todo.class));
    }

    @Test
    void 할일저장_날씨조회실패() {
        // Given
        AuthUser authUser = new AuthUser(1L, "test@example.com", UserRole.USER);
        TodoSaveRequest request = new TodoSaveRequest("제목", "내용");

        when(weatherClient.getTodayWeather()).thenThrow(new RuntimeException("날씨 조회 실패"));

        // When & Then
        assertThrows(RuntimeException.class, () -> todoService.saveTodo(authUser, request));
        verify(weatherClient).getTodayWeather();
        verify(todoRepository, never()).save(any(Todo.class));
    }

    @Test
    void 할일목록조회_성공() {
        // Given
        int page = 1;
        int size = 10;
        User user = new User("test@example.com", "password", UserRole.USER);
        List<Todo> todoList = Arrays.asList(
                createTodoWithTimestamp("제목1", "내용1", "맑음", user),
                createTodoWithTimestamp("제목2", "내용2", "흐림", user)
        );
        Page<Todo> todoPage = new PageImpl<>(todoList, PageRequest.of(page - 1, size), todoList.size());

        when(todoRepository.findAllByOrderByModifiedAtDesc(any(Pageable.class))).thenReturn(todoPage);

        // When
        Page<TodoResponse> result = todoService.getTodos(page, size);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals("제목1", result.getContent().get(0).getTitle());
        assertEquals("제목2", result.getContent().get(1).getTitle());
        assertNotNull(result.getContent().get(0).getCreatedAt());
        assertNotNull(result.getContent().get(0).getModifiedAt());

        verify(todoRepository).findAllByOrderByModifiedAtDesc(any(Pageable.class));
    }

    @Test
    void 할일목록조회_빈결과() {
        // Given
        int page = 1;
        int size = 10;
        Page<Todo> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(page - 1, size), 0);

        when(todoRepository.findAllByOrderByModifiedAtDesc(any(Pageable.class))).thenReturn(emptyPage);

        // When
        Page<TodoResponse> result = todoService.getTodos(page, size);

        // Then
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());

        verify(todoRepository).findAllByOrderByModifiedAtDesc(any(Pageable.class));
    }

    @Test
    void 할일상세조회_성공() {
        // Given
        long todoId = 1L;
        User user = new User("test@example.com", "password", UserRole.USER);
        Todo todo = createTodoWithTimestamp("제목", "내용", "맑음", user);

        when(todoRepository.findByIdWithUser(todoId)).thenReturn(Optional.of(todo));

        // When
        TodoResponse result = todoService.getTodo(todoId);

        // Then
        assertNotNull(result);
        assertEquals("제목", result.getTitle());
        assertEquals("내용", result.getContents());
        assertEquals("맑음", result.getWeather());
        assertEquals("test@example.com", result.getUser().getEmail());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getModifiedAt());

        verify(todoRepository).findByIdWithUser(todoId);
    }

    @Test
    void 할일상세조회_실패_존재하지않는할일() {
        // Given
        long todoId = 1L;

        when(todoRepository.findByIdWithUser(todoId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(InvalidRequestException.class, () -> todoService.getTodo(todoId));

        verify(todoRepository).findByIdWithUser(todoId);
    }

    // Timestamped 필드를 설정하는 헬퍼 메서드
    private void setTimestampedFields(Todo todo, LocalDateTime createdAt, LocalDateTime modifiedAt) {
        try {
            java.lang.reflect.Field createdAtField = Timestamped.class.getDeclaredField("createdAt");
            java.lang.reflect.Field modifiedAtField = Timestamped.class.getDeclaredField("modifiedAt");

            createdAtField.setAccessible(true);
            modifiedAtField.setAccessible(true);

            createdAtField.set(todo, createdAt);
            modifiedAtField.set(todo, modifiedAt);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set Timestamped fields", e);
        }
    }

    // Timestamped 필드가 설정된 Todo 객체를 생성하는 헬퍼 메서드
    private Todo createTodoWithTimestamp(String title, String contents, String weather, User user) {
        Todo todo = new Todo(title, contents, weather, user);
        LocalDateTime now = LocalDateTime.now();
        setTimestampedFields(todo, now, now);
        return todo;
    }
}