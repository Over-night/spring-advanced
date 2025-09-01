package org.example.expert.domain.todo.dto.response;

import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.user.dto.response.UserResponse;

public record TodoSaveResponse(
        Long id,
        String title,
        String contents,
        String weather,
        UserResponse user
) {
    public static TodoSaveResponse of(Todo todo, String weather, UserResponse userResponse) {
        return new TodoSaveResponse(todo.getId(), todo.getTitle(), todo.getContents(), weather, userResponse);
    }
}
