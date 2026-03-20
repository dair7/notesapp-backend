package com.notesapp.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GoogleAuthRequestDTO {

    @NotBlank(message = "El Token ID de Google no puede estar vacío")
    private String idToken;
}
