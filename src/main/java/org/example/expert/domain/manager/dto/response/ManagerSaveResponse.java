package org.example.expert.domain.manager.dto.response;

import org.example.expert.domain.manager.entity.Manager;
import org.example.expert.domain.user.dto.response.UserResponse;

public record ManagerSaveResponse(
    Long id,
    UserResponse user
){
    public static ManagerSaveResponse of(Manager manager, UserResponse user) {
        return new ManagerSaveResponse(manager.getId(), user);
    }
}