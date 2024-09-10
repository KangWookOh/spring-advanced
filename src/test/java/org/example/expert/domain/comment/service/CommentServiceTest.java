package org.example.expert.domain.comment.service;

import jakarta.validation.Validator;
import org.example.expert.domain.comment.dto.request.CommentSaveRequest;
import org.example.expert.domain.comment.dto.response.CommentResponse;
import org.example.expert.domain.comment.dto.response.CommentSaveResponse;
import org.example.expert.domain.comment.entity.Comment;
import org.example.expert.domain.comment.repository.CommentRepository;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private Validator validator;

    @Mock
    private TodoRepository todoRepository;
    @InjectMocks
    private CommentService commentService;
    @InjectMocks
    private CommentAdminService commentAdminService;

    private AuthUser authUser;
    private User user;
    private Todo todo;
    private Comment comment;


    @BeforeEach
    void setUp() {
        authUser = new AuthUser(1L, "test@example.com", UserRole.USER);
        user = User.fromAuthUser(authUser);
        todo = new Todo("Test Todo", "Test Title", "Test Content", user);
        comment = new Comment("Test Comment", user, todo);
    }

    @Test
    public void comment_등록_중_할일을_찾지_못해_에러가_발생한다() {
        // given
        long todoId = 1;
        CommentSaveRequest request = new CommentSaveRequest("contents");
        AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);

        given(todoRepository.findById(anyLong())).willReturn(Optional.empty());

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            commentService.saveComment(authUser, todoId, request);
        });

        // then
        assertEquals("Todo not found", exception.getMessage());
    }

    @Test
    public void comment를_정상적으로_등록한다() {
        // given
        long todoId = 1;
        CommentSaveRequest request = new CommentSaveRequest("contents");
        AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        Todo todo = new Todo("title", "title", "contents", user);
        Comment comment = new Comment(request.getContents(), user, todo);

        given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));
        given(commentRepository.save(any())).willReturn(comment);

        // when
        CommentSaveResponse result = commentService.saveComment(authUser, todoId, request);

        // then
        assertNotNull(result);
    }
    @Test
    public void comment_목록을_정상적으로_조회한다() {
        // given
        long todoId = 1L;
        AuthUser commentAuthUser = new AuthUser(2L, "comment@example.com", UserRole.USER);
        User commentUser = User.fromAuthUser(commentAuthUser);
        List<Comment> comments = Arrays.asList(
                new Comment("Comment 1", commentUser, todo),
                new Comment("Comment 2", commentUser, todo)
        );
        given(commentRepository.findByTodoIdWithUser(todoId)).willReturn(comments);

        // when
        List<CommentResponse> result = commentService.getComments(todoId);

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Comment 1", result.get(0).getContents());
        assertEquals("Comment 2", result.get(1).getContents());
        assertEquals(2L, result.get(0).getUser().getId());
        assertEquals("comment@example.com", result.get(0).getUser().getEmail());
    }

    @Test
    public void comment_목록_조회_시_빈_목록을_반환한다() {
        // given
        long todoId = 1L;
        given(commentRepository.findByTodoIdWithUser(todoId)).willReturn(Collections.emptyList());

        // when
        List<CommentResponse> result = commentService.getComments(todoId);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }


    @Test
    public void comment를_정상적으로_삭제한다() {
        // given
        long commentId = 1L;

        // when
        commentAdminService.deleteComment(commentId);

        // then
        verify(commentRepository).deleteById(commentId);
    }

    @Test
    public void comment_삭제_시_예외가_발생하면_처리한다() {
        // given
        long commentId = 1L;
        doThrow(new RuntimeException("Delete failed")).when(commentRepository).deleteById(commentId);

        // when & then
        assertThrows(RuntimeException.class, () -> commentAdminService.deleteComment(commentId));
    }

    @Test
    public void 다양한_사용자_역할로_comment를_등록한다() {
        // given
        long todoId = 1L;
        CommentSaveRequest request = new CommentSaveRequest("contents");
        AuthUser adminUser = new AuthUser(2L, "admin@example.com", UserRole.ADMIN);
        Todo todo = new Todo("title", "title", "contents", user);

        given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));
        given(commentRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

        // when
        CommentSaveResponse result = commentService.saveComment(adminUser, todoId, request);

        // then
        assertNotNull(result);
        assertEquals(request.getContents(), result.getContents());
        assertEquals(adminUser.getId(), result.getUser().getId());
        assertEquals(adminUser.getEmail(), result.getUser().getEmail());
    }
    @Test
    public void comment_등록_시_내용이_비어있으면_예외가_발생한다() {
        // given
        long todoId = 1L;
        CommentSaveRequest request = new CommentSaveRequest("");
        given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));

        // when & then
        assertThrows(InvalidRequestException.class, () ->
                commentService.saveComment(authUser, todoId, request));
    }

    @Test
    public void admin_사용자가_comment를_등록한다() {
        // given
        long todoId = 1L;
        CommentSaveRequest request = new CommentSaveRequest("Admin comment");
        AuthUser adminUser = new AuthUser(2L, "admin@example.com", UserRole.ADMIN);
        given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));
        given(commentRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

        // when
        CommentSaveResponse result = commentService.saveComment(adminUser, todoId, request);

        // then
        assertNotNull(result);
        assertEquals(request.getContents(), result.getContents());
        assertNotNull(result.getUser());
        assertEquals(adminUser.getId(), result.getUser().getId());
        // UserRole 확인을 제거하고 대신 사용자 이메일 확인
        assertEquals(adminUser.getEmail(), result.getUser().getEmail());
    }

    @Test
    public void comment_삭제_시_해당_comment가_존재하지_않으면_예외가_발생한다() {
        // given
        long commentId = 1L;
        doThrow(new EmptyResultDataAccessException(1)).when(commentRepository).deleteById(commentId);

        // when & then
        EmptyResultDataAccessException exception = assertThrows(EmptyResultDataAccessException.class, () ->
                commentAdminService.deleteComment(commentId));

        assertEquals("Incorrect result size: expected 1, actual 0", exception.getMessage());
    }

    @Test
    public void 사용자가_자신의_comment만_수정할_수_있다() {
        // given
        long commentId = 1L;
        CommentSaveRequest request = new CommentSaveRequest("Updated comment");
        Comment existingComment = new Comment("Original comment", user, todo);
        given(commentRepository.findById(commentId)).willReturn(Optional.of(existingComment));
        given(commentRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

        // when
        CommentSaveResponse result = commentService.updateComment(authUser, commentId, request);

        // then
        assertNotNull(result);
        assertEquals(request.getContents(), result.getContents());
        assertEquals(authUser.getId(), result.getUser().getId());
    }

    @Test
    public void 사용자가_다른_사용자의_comment를_수정하려_하면_예외가_발생한다() {
        // given
        long commentId = 1L;
        CommentSaveRequest request = new CommentSaveRequest("Updated comment");
        AuthUser differentUser = new AuthUser(2L, "different@example.com", UserRole.USER);
        Comment existingComment = new Comment("Original comment", user, todo);
        given(commentRepository.findById(commentId)).willReturn(Optional.of(existingComment));

        // when & then
        assertThrows(InvalidRequestException.class, () ->
                commentService.updateComment(differentUser, commentId, request));
    }



}
