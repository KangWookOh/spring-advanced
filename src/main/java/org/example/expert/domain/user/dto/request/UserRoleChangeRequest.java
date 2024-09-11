package org.example.expert.domain.user.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.expert.domain.user.enums.UserRole;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleChangeRequest {

    private String role;

    public UserRoleChangeRequest(UserRole userRole) {
        this.role = userRole.name();
    }
}
