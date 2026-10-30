package com.devsuj.crmdesktop.model;

/** Saldo de puntos de un cliente ({@code GET /api/clientes/{clienteId}/puntos}). Saldo = ganados − canjeados. */
public record SaldoPuntos(Integer clienteId, String clienteNombre, long puntosGanados, long puntosCanjeados, long saldo) {
}
