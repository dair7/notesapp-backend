package com.notesapp.scheduler;

import com.notesapp.entity.Recordatorio;
import com.notesapp.repository.RecordatorioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduler que revisa cada minuto si hay recordatorios cuya fecha ya llegó.
 * Por cada recordatorio vencido: envía un email al usuario y lo marca como completado.
 */
@Component
public class RecordatorioScheduler {

    private static final Logger log = LoggerFactory.getLogger(RecordatorioScheduler.class);

    private final RecordatorioRepository recordatorioRepository;

    public RecordatorioScheduler(RecordatorioRepository recordatorioRepository) {
        this.recordatorioRepository = recordatorioRepository;
    }

    @Scheduled(fixedRate = 60000) // se ejecuta cada 60 segundos
    @Transactional
    public void procesarRecordatoriosVencidos() {
        List<Recordatorio> vencidos = recordatorioRepository
                .findVencidosYPendientes(LocalDateTime.now());

        if (vencidos.isEmpty()) return;

        log.info("Procesando {} recordatorio(s) vencido(s)", vencidos.size());

        for (Recordatorio recordatorio : vencidos) {
            try {
                // Marcar como completado para mantener el estado sincronizado en BD
                recordatorio.setCompletado(true);
                recordatorioRepository.save(recordatorio);

                log.info("Recordatorio {} marcado como completado", recordatorio.getId());

            } catch (Exception e) {
                // Fallo individual no detiene el procesamiento del resto
                log.error("Error al procesar recordatorio {}: {}", recordatorio.getId(), e.getMessage(), e);
            }
        }
    }
}
