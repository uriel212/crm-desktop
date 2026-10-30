package com.devsuj.crmdesktop.service;

import com.devsuj.crmdesktop.model.Categoria;

/**
 * Lo que el usuario escribió en el formulario de producto (los números llegan como texto, sin validar).
 *
 * @param stockInicial solo al crear
 */
public record ProductoFormulario(Categoria categoria, String nombre, String descripcion, String precio,
                                 String precioPuntos, String stockMinimo, String stockInicial) {
}
