package com.devsuj.crmdesktop.model;

/** Cliente con su saldo de puntos (top del reporte de fidelización). */
public record ClienteConSaldo(Integer clienteId, String clienteNombre, long saldo) {
}
