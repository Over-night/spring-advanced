package org.example.expert.domain.todo.dto.response;

import lombok.Getter;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.user.dto.response.UserResponse;

import java.time.LocalDateTime;

public record TodoResponse (
    Long id,
    String title,
    String contents,
    String weather,
    UserResponse user,
    LocalDateTime createdAt,
    LocalDateTime modifiedAt
){
    public static TodoResponse of(Todo todo, UserResponse user) {
        return new TodoResponse(todo.getId(), todo.getTitle(), todo.getContents(), todo.getWeather(),
                user, todo.getCreatedAt(), todo.getModifiedAt());
    }
}