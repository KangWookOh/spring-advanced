package org.example.expert.domain.user;

import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.example.expert.domain.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    void 존재하는_사용자_조회시_사용자_응답_반환() {
        // Arrange
        long userId = 1L;
        User user = new User("test@example.com", "password", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", userId);  // ID 필드를 강제로 설정

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        UserResponse response = userService.getUser(userId);

        // Assert
        assertEquals(userId, response.getId());  // ID 확인
        assertEquals("test@example.com", response.getEmail());

        // Verify
        verify(userRepository).findById(userId);
    }

    @Test
    void 존재하지_않는_사용자_조회시_예외_발생() {
        // Arrange
        long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InvalidRequestException.class, () -> userService.getUser(userId));
    }

    @Test
    void 유효한_요청으로_비밀번호_변경() {
        // Arrange
        long userId = 1L;
        User user = new User("test@example.com", "oldEncodedPassword", UserRole.USER);
        UserChangePasswordRequest request = new UserChangePasswordRequest("oldPassword", "NewPassword1");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword", "oldEncodedPassword")).thenReturn(true);
        when(passwordEncoder.matches("NewPassword1", "oldEncodedPassword")).thenReturn(false);
        when(passwordEncoder.encode("NewPassword1")).thenReturn("newEncodedPassword");

        // Act
        userService.changePassword(userId, request);

        // Assert
        verify(userRepository).findById(userId);
        verify(passwordEncoder).matches("oldPassword", "oldEncodedPassword");
        verify(passwordEncoder).matches("NewPassword1", "oldEncodedPassword");
        verify(passwordEncoder).encode("NewPassword1");
        assertEquals("newEncodedPassword", user.getPassword());
    }

    @Test
    void 사용자가_없을때_비밀번호_변경시_예외_발생() {
        // Arrange
        long userId = 1L;
        UserChangePasswordRequest request = new UserChangePasswordRequest("oldPassword", "NewPassword1");
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InvalidRequestException.class, () -> userService.changePassword(userId, request));
    }

    @Test
    void 잘못된_이전_비밀번호로_변경시_예외_발생() {
        // Arrange
        long userId = 1L;
        User user = new User("test@example.com", "oldEncodedPassword", UserRole.USER);
        UserChangePasswordRequest request = new UserChangePasswordRequest("wrongPassword", "NewPassword1");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), eq("oldEncodedPassword"))).thenReturn(false);

        // Act & Assert
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> userService.changePassword(userId, request));

        assertEquals("잘못된 비밀번호입니다.", exception.getMessage());

        // Verify
        verify(userRepository).findById(userId);
        verify(passwordEncoder).matches("wrongPassword", "oldEncodedPassword");
        verify(passwordEncoder).matches("NewPassword1", "oldEncodedPassword");
        verifyNoMoreInteractions(passwordEncoder);
    }

    @Test
    void 새_비밀번호가_기존과_같을때_예외_발생() {
        // Arrange
        long userId = 1L;
        String complexPassword = "OldPassword123"; // 복잡성 요구사항을 만족하는 비밀번호
        User user = new User("test@example.com", "encodedOldPassword", UserRole.USER);
        UserChangePasswordRequest request = new UserChangePasswordRequest(complexPassword, complexPassword);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(complexPassword, "encodedOldPassword")).thenReturn(true);

        // Act & Assert
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> userService.changePassword(userId, request));

        assertEquals("새 비밀번호는 기존 비밀번호와 같을 수 없습니다.", exception.getMessage());

        // Verify
        verify(userRepository).findById(userId);
        verify(passwordEncoder).matches(complexPassword, "encodedOldPassword");
        verifyNoMoreInteractions(passwordEncoder);
    }

    @Test
    void 새_비밀번호_유효성_검사_실패시_예외_발생() {
        // Arrange
        long userId = 1L;
        UserChangePasswordRequest request = new UserChangePasswordRequest("oldPassword", "weak");

        // Act & Assert
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> userService.changePassword(userId, request));
        assertEquals("새 비밀번호는 8자 이상이어야 하고, 숫자와 대문자를 포함해야 합니다.", exception.getMessage());

        // Verify
        verifyNoInteractions(userRepository);
        verifyNoInteractions(passwordEncoder);
    }
    @Test
    void AuthUser로_User_생성_테스트() {
        // Arrange
        AuthUser authUser = new AuthUser(1L, "test@example.com", UserRole.USER);

        // Act
        User user = User.fromAuthUser(authUser);

        // Assert
        assertEquals(1L, user.getId());
        assertEquals("test@example.com", user.getEmail());
        assertEquals(UserRole.USER, user.getUserRole());
    }

    @Test
    void 사용자_역할_업데이트_테스트() {
        // Arrange
        User user = new User("test@example.com", "password", UserRole.USER);

        // Act
        user.updateRole(UserRole.ADMIN);

        // Assert
        assertEquals(UserRole.ADMIN, user.getUserRole());
    }
}