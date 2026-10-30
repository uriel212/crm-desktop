package com.devsuj.crmdesktop.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Campaña de marketing ({@code CampanaResponse}). */
public record Campana(Integer id, String nombre, CanalCampana canal, String asunto, String mensaje,
                      SegmentoCampana segmento, LocalDate fechaInicio, LocalDate fechaFin, BigDecimal presupuesto,
                      EstadoCampana estado, long totalDestinatarios) {
}
