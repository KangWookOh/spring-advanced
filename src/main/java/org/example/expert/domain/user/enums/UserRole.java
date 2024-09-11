package org.example.expert.domain.user.enums;

import lombok.Getter;
import org.example.expert.domain.common.exception.InvalidRequestException;

import java.util.Arrays;
@Getter
public enum UserRole {
    ADMIN, USER;

    public static UserRole of(String role) {
        System.out.println(role);
        return Arrays.stream(UserRole.values())
                .filter(r -> r.name().equalsIgnoreCase(role))
                .findFirst()
                .orElseThrow(() -> new InvalidRequestException("유효 하지 않은 UserRole"));


    }
}
