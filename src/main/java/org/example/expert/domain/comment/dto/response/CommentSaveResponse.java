package org.example.expert.domain.comment.dto.response;

import org.example.expert.domain.comment.entity.Comment;
import org.example.expert.domain.user.dto.response.UserResponse;

public record CommentSaveResponse (
    Long id,
    String contents,
    UserResponse user
) {
    public static CommentSaveResponse of(Comment comment, UserResponse user) {
        return new CommentSaveResponse(comment.getId(), comment.getContents(), user);
    }
}
