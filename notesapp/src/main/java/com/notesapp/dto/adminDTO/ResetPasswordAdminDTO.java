package com.notesapp.dto.adminDTO;

import com.notesapp.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordAdminDTO {

    @NotBlank(message = "La nueva contraseña es obligatoria")
    @ValidPassword
    private String nuevaPassword;
}
