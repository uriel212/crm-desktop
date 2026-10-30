package com.devsuj.crmdesktop.model;

import java.time.LocalDateTime;

/**
 * Cuerpo de {@code POST /api/clientes/{clienteId}/interacciones}: registra un contacto (REALIZADA)
 * o agenda una actividad futura (PROGRAMADA).
 *
 * @param fecha opcional si es REALIZADA (la API usa "ahora"); obligatoria y futura si es PROGRAMADA
 */
public record InteraccionRequest(TipoInteraccion tipo, EstadoInteraccion estado, LocalDateTime fecha,
                                 String asunto, String descripcion) {
}
