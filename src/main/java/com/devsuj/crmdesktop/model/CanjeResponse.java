package com.devsuj.crmdesktop.model;

/** Respuesta del canje (201): el movimiento registrado y el saldo que le queda al cliente. */
public record CanjeResponse(MovimientoPuntos movimiento, long saldo) {
}
