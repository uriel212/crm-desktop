package com.devsuj.crmdesktop.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Cuerpo de {@code POST/PUT /api/campanas} (nace PLANEADA; el estado va por PATCH). */
public record CampanaRequest(String nombre, CanalCampana canal, String asunto, String mensaje, SegmentoCampana segmento,
                             LocalDate fechaInicio, LocalDate fechaFin, BigDecimal presupuesto) {
}
