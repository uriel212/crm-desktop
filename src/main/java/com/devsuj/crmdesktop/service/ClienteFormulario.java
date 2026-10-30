package com.devsuj.crmdesktop.service;

import com.devsuj.crmdesktop.model.EstadoCliente;

/**
 * Lo que el usuario escribió en el formulario de cliente, tal cual (todavía sin validar).
 * No depende de JavaFX, así el service se puede probar sin abrir ventanas.
 */
public record ClienteFormulario(
        String nombre,
        String empresa,
        String email,
        String telefono,
        String direccion,
        EstadoCliente estado) {
}
