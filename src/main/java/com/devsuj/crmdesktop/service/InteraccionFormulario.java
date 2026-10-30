package com.devsuj.crmdesktop.service;

import com.devsuj.crmdesktop.model.Cliente;
import com.devsuj.crmdesktop.model.EstadoInteraccion;
import com.devsuj.crmdesktop.model.TipoInteraccion;

import java.time.LocalDate;

/**
 * Lo que el usuario escribió en el formulario de interacción (sin validar).
 *
 * @param cliente solo al crear
 * @param estado  solo al crear: REALIZADA (ya pasó) o PROGRAMADA (agenda)
 * @param hora    texto "HH:mm"; vacío = la hora actual en una REALIZADA de hoy
 */
public record InteraccionFormulario(Cliente cliente, TipoInteraccion tipo, EstadoInteraccion estado,
                                    LocalDate fecha, String hora, String asunto, String descripcion) {
}
