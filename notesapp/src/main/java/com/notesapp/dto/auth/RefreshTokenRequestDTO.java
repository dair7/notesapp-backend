package com.notesapp.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequestDTO {

    @NotBlank(message = "El refresh token no puede estar vacío")
    private String refreshToken;
}
