package com.devsuj.crmdesktop.model;

import java.time.LocalDateTime;

/**
 * Cliente tal como lo devuelve la API ({@code ClienteResponse}, plan del backend 1.11).
 *
 * @param vendedorId     id del vendedor asignado; {@code null} si no tiene
 * @param vendedorNombre nombre del vendedor asignado; {@code null} si no tiene
 */
public record Cliente(
        Integer id,
        String nombre,
        String empresa,
        String email,
        String telefono,
        String direccion,
        EstadoCliente estado,
        Integer vendedorId,
        String vendedorNombre,
        LocalDateTime creadoEn,
        LocalDateTime actualizadoEn) {
}
