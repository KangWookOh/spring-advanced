package org.example.expert.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse getUser(long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new InvalidRequestException("User not found"));
        return new UserResponse(user.getId(), user.getEmail());
    }

    @Transactional
    public void changePassword(long userId, UserChangePasswordRequest userChangePasswordRequest) {
        // 새 비밀번호의 유효성을 검증
        validateNewPassword(userChangePasswordRequest.getNewPassword());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidRequestException("User not found"));

        if (passwordEncoder.matches(userChangePasswordRequest.getNewPassword(), user.getPassword())) {
            throw new InvalidRequestException("새 비밀번호는 기존 비밀번호와 같을 수 없습니다.");
        }

        if (!passwordEncoder.matches(userChangePasswordRequest.getOldPassword(), user.getPassword())) {
            throw new InvalidRequestException("잘못된 비밀번호입니다.");
        }

        user.changePassword(passwordEncoder.encode(userChangePasswordRequest.getNewPassword()));
    }

    public void validateNewPassword(String newPassword) {
        if (!isPasswordLengthValid(newPassword) ||
                !containsDigit(newPassword) ||
                !containsUppercase(newPassword)) {
            throw new InvalidRequestException("새 비밀번호는 8자 이상이어야 하고, 숫자와 대문자를 포함해야 합니다.");
        }
    }

    /**
     * 비밀번호 길이가 8자 이상인지 확인
     * @param password 비밀번호
     * @return 길이 조건 만족 여부
     */
    boolean isPasswordLengthValid(String password) {
        return password.length() >= 8;
    }

    /**
     * 비밀번호에 숫자가 포함되어 있는지 확인
     * @param password 비밀번호
     * @return 숫자 포함 여부
     */
    boolean containsDigit(String password) {
        return password.matches(".*\\d.*");
    }

    /**
     * 비밀번호에 대문자가 포함되어 있는지 확인
     * @param password 비밀번호
     * @return 대문자 포함 여부
     */
     boolean containsUppercase(String password) {
        return password.matches(".*[A-Z].*");
    }
}
