package org.example.expert.domain.user.service;

import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserRoleChangeRequest;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserAdminService userAdminService;


    private User testUser;

    private UserRoleChangeRequest roleChangeRequest;

    @BeforeEach
    void setUp() {
        testUser = new User( "user@example.com", "password", UserRole.USER);
        roleChangeRequest = new UserRoleChangeRequest("ADMIN");
    }

    @Test
    void 사용자역할변경_사용자존재_성공() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        userAdminService.changeUserRole(1L, roleChangeRequest);

        assertEquals(UserRole.ADMIN, testUser.getUserRole());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void 사용자역할변경_사용자없음_예외발생() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(InvalidRequestException.class, () ->
                userAdminService.changeUserRole(1L, roleChangeRequest)
        );

        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void 사용자역할변경_잘못된역할_예외발생() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        UserRoleChangeRequest invalidRoleChangeRequest = new UserRoleChangeRequest("INVALID_ROLE");

        assertThrows(InvalidRequestException.class, () ->
                userAdminService.changeUserRole(1L, invalidRoleChangeRequest)
        );

        verify(userRepository, times(1)).findById(1L);
        assertEquals(UserRole.USER, testUser.getUserRole());
    }
}