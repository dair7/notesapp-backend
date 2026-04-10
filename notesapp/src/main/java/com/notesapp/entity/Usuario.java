package com.notesapp.entity;

import com.notesapp.enums.EstadoUsuario;
import com.notesapp.enums.RoleType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    private RoleType role = RoleType.USER;

    @Column(name = "is_verified", nullable = false, columnDefinition = "boolean default false")
    private boolean isVerified = false;

    // Estado de la cuenta: ACTIVO (puede entrar) / INACTIVO (baja lógica)
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_usuario", nullable = false, length = 20,
            columnDefinition = "varchar(20) not null default 'ACTIVO'")
    private EstadoUsuario estadoUsuario = EstadoUsuario.ACTIVO;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Se actualiza en cada login exitoso
    @Column(name = "ultima_conexion")
    private LocalDateTime ultimaConexion;
}
