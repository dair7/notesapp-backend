package com.notesapp.entity;

import com.notesapp.enums.EstadoNoteType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notas")
public class Nota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;

    @Column(name = "contenido", columnDefinition = "TEXT")
    private String contenido;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, columnDefinition = "varchar(20) default 'ACTIVA'")
    private EstadoNoteType estado = EstadoNoteType.ACTIVA;

    @Column(name = "es_anclada", nullable = false, columnDefinition = "boolean default false")
    private boolean esAnclada = false;

    // Color de fondo de la nota en formato hex (ej: "#FFE5B4"), nullable
    @Column(name = "color", length = 50)
    private String color;

    // Etiquetas separadas por coma (ej: "trabajo,personal"), nullable
    @Column(name = "etiquetas", columnDefinition = "TEXT")
    private String etiquetas;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id",  nullable = false)
    private Usuario usuario;

    // Al eliminar una nota, se eliminan sus recordatorios automáticamente
    @OneToMany(mappedBy = "nota", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Recordatorio> recordatorios;

}
