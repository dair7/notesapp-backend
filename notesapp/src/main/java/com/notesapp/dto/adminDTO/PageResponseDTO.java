package com.notesapp.dto.adminDTO;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Wrapper genérico para respuestas paginadas.
 * @param <T> tipo del contenido de cada página
 */
@Data
@NoArgsConstructor
public class PageResponseDTO<T> {

    private List<T> contenido;
    private int paginaActual;
    private int totalPaginas;
    private long totalElementos;
    private boolean esUltimaPagina;

    public PageResponseDTO(List<T> contenido, int paginaActual,
                           int totalPaginas, long totalElementos) {
        this.contenido = contenido;
        this.paginaActual = paginaActual;
        this.totalPaginas = totalPaginas;
        this.totalElementos = totalElementos;
        this.esUltimaPagina = paginaActual >= totalPaginas - 1;
    }
}
