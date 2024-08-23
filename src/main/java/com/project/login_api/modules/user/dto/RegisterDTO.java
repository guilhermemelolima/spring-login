package com.project.login_api.modules.user.dto;

import com.project.login_api.modules.user.UserRoles;

public record RegisterDTO(
        String email,
        String name,
        String password,
        UserRoles role) {}
