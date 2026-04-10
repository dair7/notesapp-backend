package com.notesapp.repository;

import com.notesapp.entity.Recordatorio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RecordatorioRepository extends JpaRepository<Recordatorio, Long> {

    // Buscar recordatorios por ID de nota
    List<Recordatorio> findByNotaId(Long notaId);

    // Buscar todos los recordatorios de un usuario (a través de la nota)
    @Query("SELECT r FROM Recordatorio r WHERE r.nota.usuario.id = :usuarioId ORDER BY r.fecha ASC")
    List<Recordatorio> findByUsuarioId(@Param("usuarioId") Long usuarioId);

    // Buscar recordatorios pendientes de un usuario
    @Query("SELECT r FROM Recordatorio r WHERE r.nota.usuario.id = :usuarioId " +
            "AND r.completado = false ORDER BY r.fecha ASC")
    List<Recordatorio> findPendientesByUsuarioId(@Param("usuarioId") Long usuarioId);

    void deleteByNotaUsuarioId(Long usuarioId);

    // Conteo por estado de completado (para estadísticas del dashboard)
    long countByCompletado(boolean completado);

    // Recordatorios cuya fecha ya llegó y aún no fueron notificados
    @Query("SELECT r FROM Recordatorio r WHERE r.fecha <= :ahora AND r.completado = false")
    List<Recordatorio> findVencidosYPendientes(@Param("ahora") LocalDateTime ahora);
}
