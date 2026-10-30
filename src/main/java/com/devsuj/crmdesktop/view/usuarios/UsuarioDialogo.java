package com.devsuj.crmdesktop.view.usuarios;

import com.devsuj.crmdesktop.model.Rol;
import com.devsuj.crmdesktop.model.Usuario;
import com.devsuj.crmdesktop.service.UsuarioFormulario;
import com.devsuj.crmdesktop.service.UsuarioService;
import com.devsuj.crmdesktop.view.comun.DialogoGuardar;
import com.devsuj.crmdesktop.view.comun.EtiquetaListCell;
import com.devsuj.crmdesktop.view.comun.Formulario;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Window;

import java.util.List;
import java.util.Optional;

/** Diálogo para crear o editar un usuario (solo ADMIN). */
public final class UsuarioDialogo {

    private UsuarioDialogo() {
    }

    /**
     * @param original usuario a editar, o {@code null} para crear uno
     * @param gerentes gerentes activos para elegir el equipo de un vendedor
     */
    public static Optional<Usuario> mostrar(Window ventana, UsuarioService service, Usuario original,
                                            List<Usuario> gerentes) {
        Formulario formulario = new Formulario();
        TextField nombre = formulario.campo("Nombre *", UsuarioService.NOMBRE, new TextField());
        TextField email = formulario.campo("Email *", UsuarioService.EMAIL, new TextField());
        PasswordField password = new PasswordField();
        if (original == null) {
            password.setPromptText("Entre 8 y 72 caracteres");
            formulario.campoAncho("Contraseña inicial *", UsuarioService.PASSWORD, password);
            formulario.regla(password, true, () -> UsuarioService.reglaPassword(password.getText()));
        }
        ComboBox<Rol> rol = formulario.campo("Rol *", UsuarioService.ROL, new ComboBox<>());
        rol.getItems().setAll(Rol.values());
        EtiquetaListCell.aplicar(rol, "Elige un rol");
        ComboBox<Usuario> gerente = formulario.campo("Gerente del equipo", UsuarioService.GERENTE, new ComboBox<>());
        gerente.getItems().add(null);
        gerente.getItems().addAll(gerentes);
        EtiquetaListCell.aplicar(gerente, "Sin gerente", Usuario::nombre);
        // Solo un VENDEDOR pertenece al equipo de un gerente.
        gerente.disableProperty().bind(rol.valueProperty().isNotEqualTo(Rol.VENDEDOR));

        formulario.regla(nombre, true, () -> UsuarioService.reglaNombre(nombre.getText()));
        formulario.regla(email, true, () -> UsuarioService.reglaEmail(email.getText()));

        if (original != null) {
            nombre.setText(original.nombre());
            email.setText(original.email());
            rol.setValue(original.rol());
            gerente.setValue(gerentes.stream().filter(g -> g.id().equals(original.gerenteId())).findFirst().orElse(null));
        } else {
            rol.setValue(Rol.VENDEDOR);
        }

        return DialogoGuardar.mostrar(ventana,
                original == null ? "Nuevo usuario" : "Editar usuario",
                original == null ? "Dar de alta a un usuario del CRM" : original.nombre(),
                formulario, "Guardar",
                () -> {
                    var datos = new UsuarioFormulario(nombre.getText(), email.getText(), password.getText(),
                            rol.getValue(), gerente.getValue());
                    return () -> service.guardar(original, datos);
                });
    }
}
