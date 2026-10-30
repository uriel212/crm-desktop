package com.devsuj.crmdesktop.service;

import com.devsuj.crmdesktop.model.Rol;
import com.devsuj.crmdesktop.model.Usuario;

/**
 * Lo que el administrador escribió en el formulario de usuario (sin validar).
 *
 * @param password solo al crear; al editar se ignora (la contraseña tiene su propio endpoint)
 * @param gerente  gerente del equipo; solo aplica a VENDEDOR
 */
public record UsuarioFormulario(String nombre, String email, String password, Rol rol, Usuario gerente) {
}
