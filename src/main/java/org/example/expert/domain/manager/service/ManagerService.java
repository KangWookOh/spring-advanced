package org.example.expert.domain.manager.service;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.example.expert.config.JwtUtil;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerResponse;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.manager.entity.Manager;
import org.example.expert.domain.manager.repository.ManagerRepository;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ManagerService {

    private final ManagerRepository managerRepository;
    private final UserRepository userRepository;
    private final TodoRepository todoRepository;
    private final JwtUtil jwtUtil;

    @Transactional
    public ManagerSaveResponse saveManager(AuthUser authUser, long todoId, ManagerSaveRequest managerSaveRequest) {
        // 일정을 만든 유저
        User user = User.fromAuthUser(authUser);
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new InvalidRequestException("Todo not found"));
        /*
         * 새롭게 추가한 부분
         * Todo의 user가 null 인지 확인 확인 후 널이면 NPE가 아닌 InvalidRequestException 을 던진다
         * */
        if(todo.getUser() == null){
            throw new InvalidRequestException("담당자를 등록하려고 하는 유저가 일정을 만든 유저가 유효하지 않습니다.");
        }

        if (!ObjectUtils.nullSafeEquals(user.getId(), todo.getUser().getId())) {
            throw new InvalidRequestException("담당자를 등록하려고 하는 유저가 일정을 만든 유저가 유효하지 않습니다.");
        }

        User managerUser = userRepository.findById(managerSaveRequest.getManagerUserId())
                .orElseThrow(() -> new InvalidRequestException("등록하려고 하는 담당자 유저가 존재하지 않습니다."));

        if (ObjectUtils.nullSafeEquals(user.getId(), managerUser.getId())) {
            throw new InvalidRequestException("일정 작성자는 본인을 담당자로 등록할 수 없습니다.");
        }

        Manager newManagerUser = new Manager(managerUser, todo);
        Manager savedManagerUser = managerRepository.save(newManagerUser);

        return new ManagerSaveResponse(
                savedManagerUser.getId(),
                new UserResponse(managerUser.getId(), managerUser.getEmail())
        );
    }

    public List<ManagerResponse> getManagers(long todoId) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new InvalidRequestException("Todo not found"));

        List<Manager> managerList = managerRepository.findByTodoIdWithUser(todo.getId());

        List<ManagerResponse> dtoList = new ArrayList<>();
        for (Manager manager : managerList) {
            User user = manager.getUser();
            dtoList.add(new ManagerResponse(
                    manager.getId(),
                    new UserResponse(user.getId(), user.getEmail())
            ));
        }
        return dtoList;
    }


    /**
     * 담당자 삭제 비즈니스 로직
     * @param todoId 할 일 ID
     * @param managerId 삭제할 담당자 ID
     */
    public void deleteManager(String bearerToken, long todoId, long managerId) {

        String token = jwtUtil.substringToken(bearerToken);
        Claims claims = jwtUtil.extractClaims(token);
        Long userId = Long.valueOf(claims.getSubject());


        // 사용자 정보 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidRequestException("유저를 찾을 수  없습니다"));

        // 할 일 정보 조회
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new InvalidRequestException("할일을 찾을 수 없습니다."));

        // 현재 사용자가 해당 할 일의 소유자인지 확인
        validateTodoOwnership(user, todo);

        // 담당자 정보 조회
        Manager manager = managerRepository.findById(managerId)
                .orElseThrow(() -> new InvalidRequestException("매니저를 찾을 수 없습니다."));

        // 할 일에 속한 담당자인지 확인
        validateManagerAssignment(todo, manager);

        // 담당자 삭제
        managerRepository.delete(manager);
    }

    /**
     * 할 일 소유권 검증
     * 현재 사용자가 해당 할 일을 소유하고 있는지 확인
     *
     * @param user 사용자 정보
     * @param todo 할 일 정보
     */
    void validateTodoOwnership(User user, Todo todo) {
        // 할 일이 존재하지 않거나, 할 일의 소유자가 현재 사용자와 다를 경우 예외 처리
        if (todo.getUser() == null || !ObjectUtils.nullSafeEquals(user.getId(), todo.getUser().getId())) {
            throw new InvalidRequestException("해당 일정을 만든 유저가 유효하지 않습니다.");
        }
    }

    /**
     * 담당자가 해당 할 일에 속해 있는지 검증
     *
     * @param todo 할 일 정보
     * @param manager 담당자 정보
     */
    private void validateManagerAssignment(Todo todo, Manager manager) {
        // 해당 담당자가 해당 할 일의 담당자인지 확인
        if (!ObjectUtils.nullSafeEquals(todo.getId(), manager.getTodo().getId())) {
            throw new InvalidRequestException("해당 일정에 등록된 담당자가 아닙니다.");
        }
    }



}
