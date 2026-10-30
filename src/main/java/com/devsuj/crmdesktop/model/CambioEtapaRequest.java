package com.devsuj.crmdesktop.model;

/** Cuerpo de {@code PATCH /api/oportunidades/{id}/etapa}. */
public record CambioEtapaRequest(EtapaOportunidad etapa, String comentario) {
}
