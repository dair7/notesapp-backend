package com.notesapp.dto.usuarioDTO;

import com.notesapp.enums.EstadoUsuario;
import com.notesapp.enums.RoleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioResponseDTO {

    private Long id;
    private String nombre;
    private String email;
    private RoleType role;
    private EstadoUsuario estadoUsuario;
    private LocalDateTime createdAt;
    private LocalDateTime ultimaConexion;
    private long cantidadNotas;
}
