package org.example.expert.domain.comment.service;

import org.example.expert.domain.comment.dto.request.CommentSaveRequest;
import org.example.expert.domain.comment.dto.response.CommentResponse;
import org.example.expert.domain.comment.dto.response.CommentSaveResponse;
import org.example.expert.domain.comment.entity.Comment;
import org.example.expert.domain.comment.repository.CommentRepository;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.common.exception.ServerException;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private TodoRepository todoRepository;
    @InjectMocks
    private CommentService commentService;

    @Test
    @DisplayName("유효하지 않은 Todo에 Comment를 저장할 때: InvalidRequestException이 발생한다")
    public void saveComment_givenInvalidTodo_throwsInvalidRequestException() {
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
    @DisplayName("Comment를 성공적으로 저장할 때: CommentSaveResponse를 반환받는다")
    public void saveComment_withValidInput_returnCommentSaveResponse() {
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
    @DisplayName("Todo에 댓글이 있을때 : dto로 매핑된 결과 리스트를 반환받는다")
    public void getComments_withExistingComments_returnCommentResponsesList() {
        // given
        long todoId = 1;

        AuthUser authUser1 = new AuthUser(1L, "email1", UserRole.USER);
        AuthUser authUser2 = new AuthUser(2L, "email2", UserRole.USER);
        User user1 = User.fromAuthUser(authUser1);
        User user2 = User.fromAuthUser(authUser2);

        Todo todo = new Todo("title", "title", "contents", user1);
        Comment comment1 = new Comment("11111", user1, todo);
        Comment comment2 = new Comment("22222", user2, todo);

        given(commentRepository.findByTodoIdWithUser(todoId)).willReturn(List.of(comment1, comment2));

        // when
        List<CommentResponse> result = commentService.getComments(todoId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).contents()).isEqualTo("11111");
        assertThat(result.get(0).user().email()).isEqualTo("email1");
        assertThat(result.get(1).contents()).isEqualTo("22222");
        assertThat(result.get(1).user().email()).isEqualTo("email2");
    }

    @Test
    @DisplayName("Todo에 댓글이 없을때 : 빈 리스트를 반환받는다")
    public void getComments_withNoComments_returnEmptyList() {
        // given
        long todoId = 1000;

        given(commentRepository.findByTodoIdWithUser(todoId)).willReturn(List.of());

        // when
        List<CommentResponse> result = commentService.getComments(todoId);

        // then
        assertThat(result).isEmpty();
    }
}
