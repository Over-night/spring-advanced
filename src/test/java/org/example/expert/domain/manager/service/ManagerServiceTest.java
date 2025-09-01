package org.example.expert.domain.manager.service;

import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerResponse;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.manager.entity.Manager;
import org.example.expert.domain.manager.repository.ManagerRepository;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ManagerServiceTest {

    @Mock
    private ManagerRepository managerRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TodoRepository todoRepository;
    @InjectMocks
    private ManagerService managerService;

    @Test
    @DisplayName("유효하지 않은 todo의 메니저 검색 시 : InvalidRequestException throw")
    public void getManagers_givenInvalidTodo_throwsInvalidRequestException() {
        // given
        long todoId = 1L;
        given(todoRepository.findById(todoId)).willReturn(Optional.empty());

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> managerService.getManagers(todoId));
        assertEquals("Todo not found", exception.getMessage());
    }

    @Test
    @DisplayName("유효한 todo의 메니저 검색 시 : 매니저 목록 dto를 반환받음 ")
    public void getManagers_withValidInput_returnManagerResponseList() {
        // given
        long todoId = 1L;
        AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        Todo todo = new Todo("title", "contents", "weather", user);
        Manager manager = new Manager(user, todo);

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(managerRepository.findByTodoIdWithUser(todo.getId())).willReturn(List.of(manager));

        // when
        List<ManagerResponse> managerResponseList = managerService.getManagers(todoId);

        // when & then
        assertThat(managerResponseList).hasSize(1);
        assertThat(managerResponseList.get(0).user().id()).isEqualTo(1L);
    }

    @Test
    @DisplayName("매니저 저장 시: todo의 user가 null일 경우 InvalidRequestException을 throw")
    void saveManager_givenTodoWithNullUser_throwsInvalidRequestException() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        long todoId = 1L;
        long managerUserId = 2L;

        Todo todo = new Todo();
        ReflectionTestUtils.setField(todo, "user", null);

        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId);

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
            managerService.saveManager(authUser, todoId, managerSaveRequest)
        );

        assertEquals("일정을 생성한 유저만 담당자를 지정할 수 있습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("매니저 저장 시: todo의 user와 요청한 user가 다르면 InvalidRequestException을 throw")
    void saveManager_givenTodoWithWrongUser_throwsInvalidRequestException() {
        // given
        AuthUser requestUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        AuthUser ownerUser = new AuthUser(2L, "b@b.com", UserRole.USER);
        User owner = User.fromAuthUser(ownerUser);

        long todoId = 1L;
        long managerUserId = 3L;

        Todo todo = new Todo("title", "contents", "weather", owner);
        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId);

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                managerService.saveManager(requestUser, todoId, managerSaveRequest)
        );

        assertEquals("일정을 생성한 유저만 담당자를 지정할 수 있습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("매니저 저장 시: 작성자가 본인을 담당자로 등록 시 InvalidRequestException을 throw")
    void saveManager_givenOwnerTriesToEntrySelf_throwsInvalidRequestException() {
        // given
        AuthUser ownerUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User owner = User.fromAuthUser(ownerUser);

        long todoId = 1L;
        long managerUserId = 1L;

        Todo todo = new Todo("title", "contents", "weather", owner);
        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId);

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(userRepository.findById(managerUserId)).willReturn(Optional.of(owner));

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                managerService.saveManager(ownerUser, todoId, managerSaveRequest)
        );

        assertEquals("일정 작성자는 본인을 담당자로 등록할 수 없습니다.", exception.getMessage());
    }


    @Test // 테스트코드 샘플
    public void manager_목록_조회에_성공한다() {
        // given
        long todoId = 1L;
        User user = new User("user1@example.com", "password", UserRole.USER);
        Todo todo = new Todo("Title", "Contents", "Sunny", user);
        ReflectionTestUtils.setField(todo, "id", todoId);

        Manager mockManager = new Manager(todo.getUser(), todo);
        List<Manager> managerList = List.of(mockManager);

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(managerRepository.findByTodoIdWithUser(todoId)).willReturn(managerList);

        // when
        List<ManagerResponse> managerResponses = managerService.getManagers(todoId);

        // then
        assertEquals(1, managerResponses.size());
        assertEquals(mockManager.getId(), managerResponses.get(0).id());
        assertEquals(mockManager.getUser().getEmail(), managerResponses.get(0).user().email());
    }

    @Test // 테스트코드 샘플
    void todo가_정상적으로_등록된다() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);  // 일정을 만든 유저

        long todoId = 1L;
        Todo todo = new Todo("Test Title", "Test Contents", "Sunny", user);

        long managerUserId = 2L;
        User managerUser = new User("b@b.com", "password", UserRole.USER);  // 매니저로 등록할 유저
        ReflectionTestUtils.setField(managerUser, "id", managerUserId);

        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId); // request dto 생성

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(userRepository.findById(managerUserId)).willReturn(Optional.of(managerUser));
        given(managerRepository.save(any(Manager.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        ManagerSaveResponse response = managerService.saveManager(authUser, todoId, managerSaveRequest);

        // then
        assertNotNull(response);
        assertEquals(managerUser.getId(), response.user().id());
        assertEquals(managerUser.getEmail(), response.user().email());
    }

    @Test
    @DisplayName("매니저 삭제 시: 유저 조회 실패 시 InvalidRequestException을 throw")
    void deleteManager_givenInvalidUser_throwsInvalidRequestException() {
        // given
        long userId = 99L;
        long todoId = 1L;
        long managerId = 1L;

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                managerService.deleteManager(userId, todoId, managerId)
        );

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    @DisplayName("매니저 삭제 시: todo 조회 실패 시 InvalidRequestException을 throw")
    void deleteManager_givenInvalidTodo_throwsInvalidRequestException() {
        // given
        long userId = 1L;
        long todoId = 99L;
        long managerId = 1L;

        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(todoRepository.findById(todoId)).willReturn(Optional.empty());

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                managerService.deleteManager(userId, todoId, managerId)
        );

        assertEquals("Todo not found", exception.getMessage());
    }

    @Test
    @DisplayName("매니저 삭제 시: todo의 user가 null일 경우 InvalidRequestException을 throw")
    void deleteManager_givenTodoWithNullUser_throwsInvalidRequestException() {
        // given
        long userId = 1L;
        long todoId = 1L;
        long managerId = 100L;

        AuthUser requestUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(requestUser);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        Todo todo = new Todo();
        ReflectionTestUtils.setField(todo, "user", null);
        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                managerService.deleteManager(userId, todoId, managerId)
        );

        assertEquals("해당 일정을 만든 유저가 유효하지 않습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("매니저 삭제 시: todo의 user와 요청자가 다를경우 InvalidRequestException을 throw")
    void deleteManager_givenTodoWithDifferentUser_throwsInvalidRequestException() {
        // given
        long userId = 1L;
        long todoId = 1L;
        long managerId = 100L;

        AuthUser requestUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(requestUser);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        AuthUser ownerUser = new AuthUser(2L, "b@b.com", UserRole.USER);
        User owner = User.fromAuthUser(ownerUser);

        Todo todo = new Todo("title", "contents", "weather", owner);
        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                managerService.deleteManager(userId, todoId, managerId)
        );

        assertEquals("해당 일정을 만든 유저가 유효하지 않습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("매니저 삭제 시: managerId 조회 결과가 없으면 InvalidRequestException을 throw")
    void deleteManager_givenInvalidManagerId_throwsInvalidRequestException() {
        // given
        long userId = 1L;
        long todoId = 1L;
        long managerId = 99L;

        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        Todo todo = new Todo("title", "contents", "weather", user);
        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));

        given(managerRepository.findById(managerId)).willReturn(Optional.empty());

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                managerService.deleteManager(userId, todoId, managerId)
        );

        assertEquals("Manager not found", exception.getMessage());
    }

    @Test
    @DisplayName("매니저 삭제 시: manager todo가 요청한 todo와 다르면 InvalidRequestException을 throw")
    void deleteManager_givenManagerNotAuthoritiesInRequestTodo_throwsInvalidRequestException() {
        // given
        long userId = 1L;
        long todoId = 1L;
        long otherTodoId = 2L;
        long managerId = 1L;

        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // 요청
        Todo requestedTodo = new Todo("title", "contents", "weather", user);
        given(todoRepository.findById(todoId)).willReturn(Optional.of(requestedTodo));

        // 실제 속한 투두
        Todo otherTodo = new Todo("other", "other", "weather2", user);
        ReflectionTestUtils.setField(otherTodo, "id", otherTodoId);

        Manager manager = new Manager(user, otherTodo);
        given(managerRepository.findById(managerId)).willReturn(Optional.of(manager));


        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                managerService.deleteManager(userId, todoId, managerId)
        );

        assertEquals("해당 일정에 등록된 담당자가 아닙니다.", exception.getMessage());
    }

    @Test
    @DisplayName("매니저 삭제 시: 모든 조건이 충족되면 managerRepository.delete가 호출된다")
    void deleteManager_withValidInput_deleteManager() {
        // given
        long userId = 1L;
        long todoId = 1L;
        long managerId = 12345L;

        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        Todo todo = new Todo("title", "contents", "weather", user);
        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));

        Manager manager = new Manager(user, todo);
        given(managerRepository.findById(managerId)).willReturn(Optional.of(manager));

        // when
        managerService.deleteManager(userId, todoId, managerId);

        // then
        verify(managerRepository).delete(manager);
    }

}
