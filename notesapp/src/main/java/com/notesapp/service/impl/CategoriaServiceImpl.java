package com.notesapp.service.impl;

import com.notesapp.dto.categoriaDTO.CategoriaRequestDTO;
import com.notesapp.dto.categoriaDTO.CategoriaResponseDTO;
import com.notesapp.entity.Categoria;
import com.notesapp.exception.ResourceNotFoundException;
import com.notesapp.repository.CategoriaRepository;
import com.notesapp.service.interfaz.CategoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Override
    public CategoriaResponseDTO createCategoria(CategoriaRequestDTO dto) {
        if (categoriaRepository.findByNombreIgnoreCase(dto.getNombre()).isPresent()) {
            throw new IllegalArgumentException("Ya existe una categoría con el nombre: " + dto.getNombre());
        }

        Categoria categoria = new Categoria();
        categoria.setNombre(dto.getNombre());
        categoria.setColorHex(dto.getColorHex());
        
        categoria = categoriaRepository.save(categoria);
        return mapToDTO(categoria);
    }

    @Override
    public CategoriaResponseDTO updateCategoria(Long id, CategoriaRequestDTO dto) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + id));

        categoriaRepository.findByNombreIgnoreCase(dto.getNombre()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new IllegalArgumentException("Ya existe otra categoría con el nombre: " + dto.getNombre());
            }
        });

        categoria.setNombre(dto.getNombre());
        categoria.setColorHex(dto.getColorHex());

        categoria = categoriaRepository.save(categoria);
        return mapToDTO(categoria);
    }

    @Override
    public void deleteCategoria(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + id));
        categoriaRepository.delete(categoria);
    }

    @Override
    public List<CategoriaResponseDTO> getAllCategorias() {
        return categoriaRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CategoriaResponseDTO getCategoriaById(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + id));
        return mapToDTO(categoria);
    }

    private CategoriaResponseDTO mapToDTO(Categoria categoria) {
        return CategoriaResponseDTO.builder()
                .id(categoria.getId())
                .nombre(categoria.getNombre())
                .colorHex(categoria.getColorHex())
                .fechaCreacion(categoria.getFechaCreacion())
                .build();
    }
}
