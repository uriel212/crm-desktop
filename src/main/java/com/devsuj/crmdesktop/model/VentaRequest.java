package com.devsuj.crmdesktop.model;

import java.util.List;

/**
 * Cuerpo de {@code POST /api/ventas} y {@code POST /api/ventas/calculo}.
 *
 * @param oportunidadId oportunidad abierta del cliente que se gana con esta venta; opcional
 * @param usuarioId     responsable (solo GERENTE/ADMIN); {@code null} = quien registra
 */
public record VentaRequest(Integer clienteId, Integer oportunidadId, Integer usuarioId, List<DetalleVentaRequest> detalles) {
}
