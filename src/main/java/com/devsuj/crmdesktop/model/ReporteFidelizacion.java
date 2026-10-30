package com.devsuj.crmdesktop.model;

import java.util.List;

/** Reporte de fidelización ({@code GET /api/reportes/fidelizacion}) de los clientes del alcance. */
public record ReporteFidelizacion(long puntosEmitidos, long puntosCanjeados, long saldoEnCirculacion, long clientesConSaldo,
                                  List<ClienteConSaldo> topClientes) {

    public ReporteFidelizacion {
        topClientes = topClientes == null ? List.of() : List.copyOf(topClientes);
    }
}
