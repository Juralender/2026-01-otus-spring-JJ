package com.kshaitry.userlist.dto;

import com.kshaitry.userlist.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(
        @NotBlank @Size(min = 3, max = 100) String username,
        @NotBlank @Size(min = 6, max = 100) String password,
        String fullName,
        String email,
        Role role
) {
}
