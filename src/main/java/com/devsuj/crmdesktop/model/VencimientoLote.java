package com.devsuj.crmdesktop.model;

import java.util.List;

/** Respuesta de {@code POST /api/contratos/vencimientos}. */
public record VencimientoLote(int vencidos, List<Integer> contratoIds) {
}
