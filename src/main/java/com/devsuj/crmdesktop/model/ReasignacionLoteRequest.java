package com.devsuj.crmdesktop.model;

import java.util.List;

/** Cuerpo de {@code POST /api/clientes/reasignaciones}: varios clientes pasan a un mismo responsable. */
public record ReasignacionLoteRequest(List<Integer> clienteIds, Integer vendedorId) {
}
