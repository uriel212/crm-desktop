package com.devsuj.crmdesktop.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Vista 360 de un cliente ({@code GET /api/clientes/{id}/resumen}).
 *
 * @param ultimaCompra      {@code null} si nunca ha comprado
 * @param ultimaInteraccion {@code null} si no hay interacciones
 * @param campanas          campañas en las que ha sido destinatario
 */
public record ResumenCliente(Cliente cliente, long numeroCompras, BigDecimal totalComprado, BigDecimal ticketPromedio,
                             LocalDateTime ultimaCompra, List<VentaResumen> ultimasVentas, long oportunidadesAbiertas,
                             BigDecimal montoPipeline, LocalDateTime ultimaInteraccion, long actividadesProgramadas,
                             long saldoPuntos, long contratosVigentes, long ticketsAbiertos, long campanas) {

    public ResumenCliente {
        ultimasVentas = ultimasVentas == null ? List.of() : List.copyOf(ultimasVentas);
    }
}
