package com.notesapp.scheduler;

import com.notesapp.entity.Recordatorio;
import com.notesapp.repository.RecordatorioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Scheduler que revisa periódicamente recordatorios vencidos y los registra en log.
 * NO marca automáticamente como completados — esa acción es exclusiva del usuario.
 * Las notificaciones locales son responsabilidad de la app Flutter.
 */
@Component
public class RecordatorioScheduler {

    private static final Logger log = LoggerFactory.getLogger(RecordatorioScheduler.class);

    private final RecordatorioRepository recordatorioRepository;

    public RecordatorioScheduler(RecordatorioRepository recordatorioRepository) {
        this.recordatorioRepository = recordatorioRepository;
    }

    @Scheduled(fixedRate = 60000) // se ejecuta cada 60 segundos
    @Transactional(readOnly = true)
    public void reportarRecordatoriosVencidos() {
        // Usar UTC para que coincida con las fechas almacenadas en UTC
        List<Recordatorio> vencidos = recordatorioRepository
                .findVencidosYPendientes(LocalDateTime.now(ZoneOffset.UTC));

        if (!vencidos.isEmpty()) {
            log.info("{} recordatorio(s) vencido(s) pendiente(s) de acción del usuario", vencidos.size());
        }
    }
}
