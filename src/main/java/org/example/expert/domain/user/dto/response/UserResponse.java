package org.example.expert.domain.user.dto.response;

import org.example.expert.domain.user.entity.User;

public record UserResponse(
        Long id,
        String email
) {
    public static UserResponse of(User user) {
        return new UserResponse(user.getId(), user.getEmail());
    }
}
