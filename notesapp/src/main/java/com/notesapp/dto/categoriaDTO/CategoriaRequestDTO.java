package com.notesapp.dto.categoriaDTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoriaRequestDTO {
    @NotBlank(message = "El nombre de la categoría es obligatorio")
    private String nombre;

    private String colorHex;
}
