package com.notesapp.dto.notaDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotaRequestDTO {

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 200, message = "El título no puede superar los 200 caracteres")
    private String titulo;

    private String contenido;

    // Color de fondo en formato hex (ej: "#FFE5B4"), opcional
    private String color;

    // Etiquetas separadas por coma, opcional
    private String etiquetas;

    // ID de la categoría a la que pertenece la nota, opcional
    private Long categoriaId;
}
