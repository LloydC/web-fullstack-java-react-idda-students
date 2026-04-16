package com.ironhack.ironboard.mapper;

import com.ironhack.ironboard.dto.response.UserResponse;
import com.ironhack.ironboard.entity.User;

public class UserMapper {

    private UserMapper() {}

    public static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().name()
        );
    }
}
