package com.devsuj.crmdesktop.service;

import com.devsuj.crmdesktop.model.CanalCampana;
import com.devsuj.crmdesktop.model.SegmentoCampana;

import java.time.LocalDate;

/** Lo que el usuario escribió en el formulario de campaña (sin validar; el presupuesto como texto). */
public record CampanaFormulario(String nombre, CanalCampana canal, String asunto, String mensaje,
                                SegmentoCampana segmento, LocalDate fechaInicio, LocalDate fechaFin, String presupuesto) {
}
