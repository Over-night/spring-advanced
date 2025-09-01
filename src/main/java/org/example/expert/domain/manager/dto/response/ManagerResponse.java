package org.example.expert.domain.manager.dto.response;

import org.example.expert.domain.manager.entity.Manager;
import org.example.expert.domain.user.dto.response.UserResponse;

public record ManagerResponse (
    Long id,
    UserResponse user
){
    public static ManagerResponse of(Manager manager, UserResponse user) {
        return new ManagerResponse(manager.getId(), user);
    }
}
