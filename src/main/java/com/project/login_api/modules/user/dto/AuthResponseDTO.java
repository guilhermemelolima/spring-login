package com.project.login_api.modules.user.dto;

public record AuthResponseDTO(
        UserDTO userDTO,
        String token) {
}
