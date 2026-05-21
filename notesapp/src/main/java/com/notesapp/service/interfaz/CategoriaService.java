package com.notesapp.service.interfaz;

import com.notesapp.dto.categoriaDTO.CategoriaRequestDTO;
import com.notesapp.dto.categoriaDTO.CategoriaResponseDTO;

import java.util.List;

public interface CategoriaService {
    CategoriaResponseDTO createCategoria(CategoriaRequestDTO dto);
    CategoriaResponseDTO updateCategoria(Long id, CategoriaRequestDTO dto);
    void deleteCategoria(Long id);
    List<CategoriaResponseDTO> getAllCategorias();
    CategoriaResponseDTO getCategoriaById(Long id);
}
