package com.devsuj.crmdesktop.view.usuarios;

import com.devsuj.crmdesktop.model.Usuario;
import com.devsuj.crmdesktop.service.UsuarioService;
import com.devsuj.crmdesktop.view.comun.DialogoGuardar;
import com.devsuj.crmdesktop.view.comun.Formulario;
import javafx.scene.control.PasswordField;
import javafx.stage.Window;

/**
 * Diálogo para cambiar una contraseña: la propia (pide la actual) o la de otro usuario (solo ADMIN).
 */
public final class PasswordDialogo {

    private PasswordDialogo() {
    }

    /** @return {@code true} si se cambió */
    public static boolean mostrar(Window ventana, UsuarioService service, Usuario usuario, boolean esPropia) {
        Formulario formulario = new Formulario();
        PasswordField actual = new PasswordField();
        if (esPropia) {
            formulario.campoAncho("Contraseña actual *", UsuarioService.PASSWORD_ACTUAL, actual);
        }
        PasswordField nueva = formulario.campo("Nueva contraseña *", UsuarioService.PASSWORD_NUEVA, new PasswordField());
        PasswordField confirmacion = formulario.campo("Repite la nueva contraseña *", UsuarioService.CONFIRMACION,
                new PasswordField());
        nueva.setPromptText("Entre 8 y 72 caracteres");
        formulario.regla(nueva, true, () -> UsuarioService.reglaPassword(nueva.getText()));

        return DialogoGuardar.mostrar(ventana, "Cambiar contraseña",
                esPropia ? "Cambia tu contraseña" : "Nueva contraseña para " + usuario.nombre(),
                formulario, "Cambiar contraseña",
                () -> {
                    String textoActual = actual.getText();
                    String textoNueva = nueva.getText();
                    String textoConfirmacion = confirmacion.getText();
                    return () -> {
                        service.cambiarPassword(usuario, esPropia, textoActual, textoNueva, textoConfirmacion);
                        return Boolean.TRUE;
                    };
                }).isPresent();
    }
}
