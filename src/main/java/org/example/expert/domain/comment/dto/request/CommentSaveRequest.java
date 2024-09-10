package org.example.expert.domain.comment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommentSaveRequest {
    private static final int MAX_COMMENT_LENGTH = 1000;

    @NotBlank
    @Size(max = MAX_COMMENT_LENGTH, message = "Comment content exceeds maximum length of " + MAX_COMMENT_LENGTH)

    private String contents;
}
