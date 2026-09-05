package com.kshaitry.userlist.dto;

import com.kshaitry.userlist.entity.Role;
import com.kshaitry.userlist.entity.User;

public record UserResponse(Long id, String username, String fullName, String email, Role role) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getFullName(), user.getEmail(), user.getRole());
    }
}
