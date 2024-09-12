package org.example.expert.domain.manager.service;
import org.example.expert.config.JwtUtil;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerResponse;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.manager.entity.Manager;
import org.example.expert.domain.manager.repository.ManagerRepository;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import io.jsonwebtoken.Claims;


import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ManagerServiceTest {

    @Mock
    private ManagerRepository managerRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TodoRepository todoRepository;
    @Mock
    private JwtUtil jwtUtil;
    @InjectMocks
    private ManagerService managerService;


    @Test
    public void manager_목록_조회_시_Todo가_없다면_X_에러를_던진다() {
        // given
        long todoId = 1L;
        given(todoRepository.findById(todoId)).willReturn(Optional.empty());

        // when & then
        /*
        * 중괄호를 사용 명시적으로 블록 정의했고 중괄호로 감싼 표현식이 되며
        * void 타입이든 반환값이 표현식이든 여러줄에 코드가 포함될수 있다.
        * */
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                managerService.getManagers(todoId));
        assertEquals("Todo not found", exception.getMessage());
    }

    @Test
    void todo의_user가_null인_경우_예외가_발생한다() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        long todoId = 1L;
        long managerUserId = 2L;

        Todo todo = new Todo();
        ReflectionTestUtils.setField(todo, "user", null);

        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId);

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
            managerService.saveManager(authUser, todoId, managerSaveRequest)
        );

        assertEquals("담당자를 등록하려고 하는 유저가 일정을 만든 유저가 유효하지 않습니다.", exception.getMessage());
    }

    @Test // 테스트코드 샘플
    public void manager_목록_조회에_성공한다() {
        // given
        long todoId = 1L;
        User user = new User("user1@example.com", "password", UserRole.USER);
        Todo todo = new Todo("Title", "Contents", "Sunny", user);
        ReflectionTestUtils.setField(todo, "id", todoId);

        Manager mockManager = new Manager(todo.getUser(), todo);
        List<Manager> managerList = List.of(mockManager);

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(managerRepository.findByTodoIdWithUser(todoId)).willReturn(managerList);

        // when
        List<ManagerResponse> managerResponses = managerService.getManagers(todoId);

        // then
        assertEquals(1, managerResponses.size());
        assertEquals(mockManager.getId(), managerResponses.get(0).getId());
        assertEquals(mockManager.getUser().getEmail(), managerResponses.get(0).getUser().getEmail());
    }

    @Test
    void todo가_정상적으로_등록된다() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);  // 일정을 만든 유저

        long todoId = 1L;
        Todo todo = new Todo("Test Title", "Test Contents", "Sunny", user);

        long managerUserId = 2L;
        User managerUser = new User("b@b.com", "password", UserRole.USER);  // 매니저로 등록할 유저
        ReflectionTestUtils.setField(managerUser, "id", managerUserId);

        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId); // request dto 생성

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(userRepository.findById(managerUserId)).willReturn(Optional.of(managerUser));
        given(managerRepository.save(any(Manager.class))).willAnswer(invocation -> invocation.getArgument(0));
        // when
        ManagerSaveResponse response = managerService.saveManager(authUser, todoId, managerSaveRequest);
        // then
        assertNotNull(response);
        assertEquals(managerUser.getId(), response.getUser().getId());
        assertEquals(managerUser.getEmail(), response.getUser().getEmail());
    }

    @Test
    void 일정_작성자가_본인을_담당자로_등록할_수_없다() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        long todoId = 1L;
        Todo todo = new Todo("Test Title", "Test Contents", "Sunny", user);
        ManagerSaveRequest request = new ManagerSaveRequest(authUser.getId());

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(userRepository.findById(authUser.getId())).willReturn(Optional.of(user));

        // when & then
        assertThrows(InvalidRequestException.class, () ->
                managerService.saveManager(authUser, todoId, request)
        );
    }

    @Test
    void 존재하지_않는_유저를_담당자로_등록할_수_없다() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        long todoId = 1L;
        Todo todo = new Todo("Test Title", "Test Contents", "Sunny", user);
        long nonExistentUserId = 999L;
        ManagerSaveRequest request = new ManagerSaveRequest(nonExistentUserId);

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(userRepository.findById(nonExistentUserId)).willReturn(Optional.empty());

        // when & then
        assertThrows(InvalidRequestException.class, () ->
                managerService.saveManager(authUser, todoId, request)
        );
    }

    @Test
    void deleteManager_성공() {
        // given
        String token = "Bearer validToken";
        String strippedToken = "validToken";
        long todoId = 1L;
        long managerId = 2L;
        long userId = 3L;

        User user = new User("user@example.com", "password", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", userId);

        Todo todo = new Todo("Test Todo", "Description", "Sunny", user);
        ReflectionTestUtils.setField(todo, "id", todoId);

        User managerUser = new User("manager@example.com", "password", UserRole.USER);
        ReflectionTestUtils.setField(managerUser, "id", 4L);

        Manager manager = new Manager(managerUser, todo);
        ReflectionTestUtils.setField(manager, "id", managerId);

        // Mocking JwtUtil behavior
        given(jwtUtil.substringToken(token)).willReturn(strippedToken);
        Claims mockClaims = mock(Claims.class);
        given(mockClaims.getSubject()).willReturn(String.valueOf(userId));
        given(jwtUtil.extractClaims(strippedToken)).willReturn(mockClaims);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(managerRepository.findById(managerId)).willReturn(Optional.of(manager));

        // when
        assertDoesNotThrow(() -> managerService.deleteManager(token, todoId, managerId));

        // then
        verify(managerRepository).delete(manager);
    }
    @Test
    void deleteManager_할일을_찾을_수_없음() {
        // given
        String token = "Bearer validToken";
        long todoId = 1L;
        long managerId = 2L;
        long userId = 3L;

        User user = new User("user@example.com", "password", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", userId);

        Claims claims = Mockito.mock(Claims.class);

        given(jwtUtil.substringToken(token)).willReturn("validToken");
        given(jwtUtil.extractClaims("validToken")).willReturn(claims);
        given(claims.getSubject()).willReturn(String.valueOf(userId));
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(todoRepository.findById(todoId)).willReturn(Optional.empty());

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> managerService.deleteManager(token, todoId, managerId));
        assertEquals("할일을 찾을 수 없습니다.", exception.getMessage());
    }
    @Test
    void deleteManager_매니저를_찾을_수_없음() {
        // given
        String token = "Bearer validToken";
        long todoId = 1L;
        long managerId = 2L;
        long userId = 3L;

        User user = new User("user@example.com", "password", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", userId);

        Todo todo = new Todo("Test Todo", "Description", "Sunny", user);
        ReflectionTestUtils.setField(todo, "id", todoId);

        Claims claims = Mockito.mock(Claims.class);

        given(jwtUtil.substringToken(token)).willReturn("validToken");
        given(jwtUtil.extractClaims("validToken")).willReturn(claims);
        given(claims.getSubject()).willReturn(String.valueOf(userId));
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(managerRepository.findById(managerId)).willReturn(Optional.empty());

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> managerService.deleteManager(token, todoId, managerId));
        assertEquals("매니저를 찾을 수 없습니다.", exception.getMessage());
    }
    @Test
    void deleteManager_할일_소유자가_아님() {
        // given
        String token = "Bearer validToken";
        long todoId = 1L;
        long managerId = 2L;
        long userId = 3L;

        User user = new User("user@example.com", "password", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", userId);

        User todoOwner = new User("owner@example.com", "password", UserRole.USER);
        ReflectionTestUtils.setField(todoOwner, "id", 4L);

        Todo todo = new Todo("Test Todo", "Description", "Sunny", todoOwner);
        ReflectionTestUtils.setField(todo, "id", todoId);

        Claims claims = Mockito.mock(Claims.class);

        given(jwtUtil.substringToken(token)).willReturn("validToken");
        given(jwtUtil.extractClaims("validToken")).willReturn(claims);
        given(claims.getSubject()).willReturn(String.valueOf(userId));
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> managerService.deleteManager(token, todoId, managerId));
        assertEquals("해당 일정을 만든 유저가 유효하지 않습니다.", exception.getMessage());
    }
    @Test
    void deleteManager_매니저가_해당_할일에_속하지_않음() {
        // given
        String token = "Bearer validToken";
        long todoId = 1L;
        long managerId = 2L;
        long userId = 3L;

        User user = new User("user@example.com", "password", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", userId);

        Todo todo = new Todo("Test Todo", "Description", "Sunny", user);
        ReflectionTestUtils.setField(todo, "id", todoId);

        Todo anotherTodo = new Todo("Another Todo", "Description", "Rainy", user);
        ReflectionTestUtils.setField(anotherTodo, "id", 4L);

        User managerUser = new User("manager@example.com", "password", UserRole.USER);
        ReflectionTestUtils.setField(managerUser, "id", 5L);

        Manager manager = new Manager(managerUser, anotherTodo);
        ReflectionTestUtils.setField(manager, "id", managerId);

        Claims claims = Mockito.mock(Claims.class);

        given(jwtUtil.substringToken(token)).willReturn("validToken");
        given(jwtUtil.extractClaims("validToken")).willReturn(claims);
        given(claims.getSubject()).willReturn(String.valueOf(userId));
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(managerRepository.findById(managerId)).willReturn(Optional.of(manager));

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> managerService.deleteManager(token, todoId, managerId));
        assertEquals("해당 일정에 등록된 담당자가 아닙니다.", exception.getMessage());
    }

}
