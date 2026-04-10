package com.notesapp.dto.auth;

import com.notesapp.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordRequestDTO {

    @NotBlank(message = "El código de recuperación es obligatorio")
    private String token;

    @NotBlank(message = "La nueva contraseña es obligatoria")
    @ValidPassword
    private String newPassword;
}
