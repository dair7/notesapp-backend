package com.notesapp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "recordatorios")
public class Recordatorio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(columnDefinition = "boolean default false")
    private Boolean completado = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(255) default 'MEDIA'")
    private Prioridad prioridad = Prioridad.MEDIA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nota_id", nullable = false)
    private Nota nota;
}
