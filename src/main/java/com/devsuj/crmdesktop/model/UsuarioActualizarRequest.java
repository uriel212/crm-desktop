package com.devsuj.crmdesktop.model;

/** Cuerpo de {@code PUT /api/usuarios/{id}} (la contraseña y el estado tienen su propio endpoint). */
public record UsuarioActualizarRequest(String nombre, String email, Rol rol, Integer gerenteId) {
}
