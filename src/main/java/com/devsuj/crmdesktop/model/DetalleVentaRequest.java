package com.devsuj.crmdesktop.model;

/** Renglón que se envía a la API: solo producto y cantidad (el frontend NUNCA envía precios). */
public record DetalleVentaRequest(Integer productoId, Integer cantidad) {
}
