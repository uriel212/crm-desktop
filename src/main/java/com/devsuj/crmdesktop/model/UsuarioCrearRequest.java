package com.devsuj.crmdesktop.model;

/** Cuerpo de {@code POST /api/usuarios}. {@code gerenteId} solo aplica a VENDEDOR. */
public record UsuarioCrearRequest(String nombre, String email, String password, Rol rol, Integer gerenteId) {
}
