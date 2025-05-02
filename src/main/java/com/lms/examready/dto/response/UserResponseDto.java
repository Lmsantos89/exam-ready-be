package com.lms.examready.dto.response;

import com.lms.examready.model.Role;
import com.lms.examready.model.User;

import java.time.LocalDateTime;

public record UserResponseDto(
        String username,
        String email,
        Role role,
        LocalDateTime createdAt
) {

    public static UserResponseDto from(User user) {
        return new UserResponseDto(user.getUsername(), user.getEmail(), user.getRole(), user.getCreatedAt());
    }
}
