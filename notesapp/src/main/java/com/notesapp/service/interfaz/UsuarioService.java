package com.notesapp.service.interfaz;

import com.notesapp.dto.usuarioDTO.UsuarioRequestDTO;
import com.notesapp.dto.usuarioDTO.UsuarioResponseDTO;
import com.notesapp.enums.RoleType;

import java.util.List;

public interface UsuarioService {

    UsuarioResponseDTO crearUsuario(UsuarioRequestDTO dto);

    UsuarioResponseDTO crearAdmin(UsuarioRequestDTO dto);

    List<UsuarioResponseDTO> obtenerTodos();

    UsuarioResponseDTO obtenerPorId(Long id);

    UsuarioResponseDTO actualizarUsuario(Long id, UsuarioRequestDTO dto);

    UsuarioResponseDTO cambiarRol(Long id, RoleType nuevoRol);

    void eliminarUsuario(Long id);
}