package org.example.expert.domain.comment.service;
import org.example.expert.domain.comment.controller.CommentAdminController;
import org.example.expert.domain.comment.dto.response.CommentResponse;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
public class CommentAdminControllerTest {


    @Mock
    private CommentAdminService commentAdminService;

    @InjectMocks
    private CommentAdminController commentAdminController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(commentAdminController).build();
    }

    @Test
    void deleteComment_성공적으로_댓글을_삭제한다() throws Exception {
        // Given
        long commentId = 1L;
        doNothing().when(commentAdminService).deleteComment(commentId);

        // When & Then
        mockMvc.perform(delete("/admin/comments/{commentId}", commentId))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(commentAdminService, times(1)).deleteComment(commentId);
    }




}