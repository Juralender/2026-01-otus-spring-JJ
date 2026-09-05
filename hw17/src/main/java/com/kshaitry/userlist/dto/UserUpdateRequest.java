package com.kshaitry.userlist.dto;

import com.kshaitry.userlist.entity.Role;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        String fullName,
        String email,
        Role role,
        @Size(min = 6, max = 100) String password
) {
}
