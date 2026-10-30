package com.devsuj.crmdesktop.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Renglón del listado {@code GET /api/ventas} (sin detalle). Una venta no tiene estados: solo existe si se concretó. */
public record VentaResumen(Integer id, Integer clienteId, String clienteNombre, Integer usuarioId, String usuarioNombre,
                           Integer oportunidadId, LocalDateTime fecha, BigDecimal subtotal, BigDecimal descuento,
                           BigDecimal total) {
}
