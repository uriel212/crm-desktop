package com.devsuj.crmdesktop.model;

/** Destinatario de una campaña ({@code DestinatarioResponse}). */
public record Destinatario(Integer campanaId, Integer clienteId, String clienteNombre, String clienteEmail,
                           Integer vendedorId, String vendedorNombre, ResultadoCampana resultado) {
}
