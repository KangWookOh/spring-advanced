package org.example.expert.domain.comment.service;

import org.example.expert.domain.comment.controller.CommentAdminController;
import org.example.expert.domain.comment.controller.CommentController;
import org.example.expert.domain.comment.dto.request.CommentSaveRequest;
import org.example.expert.domain.comment.dto.response.CommentResponse;
import org.example.expert.domain.comment.dto.response.CommentSaveResponse;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CommentControllerTest {

    @Mock
    private CommentService commentService;

    @InjectMocks
    private CommentController commentController;

    @InjectMocks
    private CommentAdminController commentAdminController;

    @Mock
    private CommentAdminService commentAdminService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void saveComment_정상적으로_댓글을_저장한다() {
        // Given
        AuthUser authUser = new AuthUser(1L, "test@example.com", UserRole.USER);
        long todoId = 1L;
        CommentSaveRequest request = new CommentSaveRequest("테스트 댓글");
        UserResponse userResponse = new UserResponse(1L, "test@example.com");
        CommentSaveResponse expectedResponse = new CommentSaveResponse(1L, "테스트 댓글", userResponse);

        when(commentService.saveComment(authUser, todoId, request)).thenReturn(expectedResponse);

        // When
        ResponseEntity<CommentSaveResponse> response = commentController.saveComment(authUser, todoId, request);

        // Then
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expectedResponse, response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("테스트 댓글", response.getBody().getContents());
        assertEquals(userResponse, response.getBody().getUser());
        verify(commentService, times(1)).saveComment(authUser, todoId, request);

    }
    @Test
    void getComments_모든_댓글을_반환한다() {
        // Given
        long todoId = 1L;
        UserResponse userResponse = new UserResponse(1L, "test@example.com");
        List<CommentResponse> expectedComments = Arrays.asList(
                new CommentResponse(1L, "첫 번째 댓글", userResponse),
                new CommentResponse(2L, "두 번째 댓글", userResponse)
        );

        when(commentService.getComments(todoId)).thenReturn(expectedComments);

        // When
        ResponseEntity<List<CommentResponse>> response = commentController.getComments(todoId);

        // Then
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expectedComments, response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals(1L, response.getBody().get(0).getId());
        assertEquals("첫 번째 댓글", response.getBody().get(0).getContents());
        assertEquals(userResponse, response.getBody().get(0).getUser());
        verify(commentService, times(1)).getComments(todoId);
    }
    @Test
    void saveComment_관리자_권한으로_댓글을_저장한다() {
        // Given
        AuthUser authUser = new AuthUser(1L, "admin@example.com", UserRole.ADMIN);
        long todoId = 1L;
        CommentSaveRequest request = new CommentSaveRequest("관리자 댓글");
        UserResponse userResponse = new UserResponse(1L, "admin@example.com");
        CommentSaveResponse expectedResponse = new CommentSaveResponse(1L, "관리자 댓글", userResponse);

        when(commentService.saveComment(authUser, todoId, request)).thenReturn(expectedResponse);

        // When
        ResponseEntity<CommentSaveResponse> response = commentController.saveComment(authUser, todoId, request);

        // Then
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expectedResponse, response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("관리자 댓글", response.getBody().getContents());
        assertEquals(userResponse, response.getBody().getUser());
        verify(commentService, times(1)).saveComment(authUser, todoId, request);
    }

}
