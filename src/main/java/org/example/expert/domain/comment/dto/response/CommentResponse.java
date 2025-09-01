package org.example.expert.domain.comment.dto.response;

import org.example.expert.domain.comment.entity.Comment;
import org.example.expert.domain.user.dto.response.UserResponse;

public record CommentResponse (
    Long id,
    String contents,
    UserResponse user
) {
    public static CommentResponse of(Comment comment, UserResponse user) {
        return new CommentResponse(comment.getId(), comment.getContents(), user);
    }
}
