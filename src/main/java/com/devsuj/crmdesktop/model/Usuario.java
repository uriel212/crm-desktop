package com.devsuj.crmdesktop.model;

import java.time.LocalDateTime;

/**
 * Usuario del sistema tal como lo devuelve la API ({@code UsuarioResponse}). Nunca trae la contraseña.
 *
 * @param gerenteId gerente del equipo (solo para VENDEDOR); {@code null} si no tiene
 */
public record Usuario(
        Integer id,
        String nombre,
        String email,
        Rol rol,
        Integer gerenteId,
        String gerenteNombre,
        boolean activo,
        LocalDateTime creadoEn) {

    /** Texto para listas y combos: "Carla Mendoza (Vendedor)". */
    public String nombreConRol() {
        return nombre + " (" + rol.etiqueta() + ")";
    }
}
