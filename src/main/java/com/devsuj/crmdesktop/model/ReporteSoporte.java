package com.devsuj.crmdesktop.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * Reporte de soporte ({@code GET /api/reportes/soporte}), según el alcance del usuario.
 *
 * @param horasPromedioCierre {@code null} si no hay tickets cerrados
 */
public record ReporteSoporte(long abiertos, long enProceso, long cerrados, long sinAsignar, BigDecimal horasPromedioCierre,
                             List<TicketsPorPrioridad> pendientesPorPrioridad) {

    public ReporteSoporte {
        pendientesPorPrioridad = pendientesPorPrioridad == null ? List.of() : List.copyOf(pendientesPorPrioridad);
    }
}
