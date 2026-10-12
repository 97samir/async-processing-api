package com.asyncprocessing.platform.task.service;
// generador de resultados para no recargar la logica de trabajo
import com.asyncprocessing.platform.task.entity.ProcessingTask;
import org.springframework.stereotype.Service;

@Service
public class ReportGenerationService {

    public String generateSummary(ProcessingTask task) {

        if (task.getDescription() == null
                || task.getDescription().isBlank()) {
            throw new IllegalArgumentException(
                    "La descripción del reporte está vacía"
            );
        }

        /* provocar un error para pruebas */
        if (task.getDescription().contains("[PRUEBA-FALLO]")) {
            throw new IllegalStateException(
                    "Fallo simulado para comprobar los reintentos"
            );
        }

        // Simulación temporal de la generación del reporte.
        return "Resumen generado correctamente para: "
                + task.getDescription();
    }
}
