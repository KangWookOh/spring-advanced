package org.example.expert.domain.manager.controller;

import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.expert.config.JwtUtil;
import org.example.expert.domain.common.annotation.Auth;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerResponse;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.manager.service.ManagerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ManagerController {

    private final ManagerService managerService;
    private final JwtUtil jwtUtil;

    @PostMapping("/todos/{todoId}/managers")
    public ResponseEntity<ManagerSaveResponse> saveManager(
            @Auth AuthUser authUser,
            @PathVariable long todoId,
            @Valid @RequestBody ManagerSaveRequest managerSaveRequest
    ) {
        return ResponseEntity.ok(managerService.saveManager(authUser, todoId, managerSaveRequest));
    }

    @GetMapping("/todos/{todoId}/managers")
    public ResponseEntity<List<ManagerResponse>> getMembers(@PathVariable long todoId) {
        return ResponseEntity.ok(managerService.getManagers(todoId));
    }

    /**
     * 담당자 삭제 요청 처리
     *
     * @param bearerToken JWT 토큰 (Authorization 헤더에서 전달)
     * @param todoId 할 일 ID
     * @param managerId 삭제할 담당자 ID
     * 컨트롤러에서 JWT 처리 로직을 호출하는 대신 사용자 정보는 서비스 레이어에서 처리되도록
     * 변경ㄹ Authorization 헤더는 그대로 전달하고 토큰 처리오ㅓㅏ 사용자 검증은 ManagerService에서 처리하도록 변경
     */
    @DeleteMapping("/{todoId}/managers/{managerId}")
    public void deleteManager(
            @RequestHeader("Authorization") String bearerToken,
            @PathVariable long todoId,
            @PathVariable long managerId
    ) {
        // 서비스 레이얼포 토큰과 ID 정보만 전달 ,JWT 처리는 서비스에서 처리 하게 분리
        managerService.deleteManager(bearerToken,todoId,managerId);

    }
}
