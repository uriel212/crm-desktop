package com.devsuj.crmdesktop.view.clientes;

import com.devsuj.crmdesktop.model.Cliente;
import com.devsuj.crmdesktop.service.ClienteFormulario;
import com.devsuj.crmdesktop.service.ClienteService;
import com.devsuj.crmdesktop.util.Vistas;
import com.devsuj.crmdesktop.view.comun.DialogoGuardar;
import javafx.scene.Parent;
import javafx.stage.Window;

import java.util.Optional;

/**
 * Ventana modal para crear o editar un cliente.
 *
 * <p>El comportamiento de "Guardar" (segundo plano, no cerrar si hay errores, Enter/Esc) lo da
 * {@link DialogoGuardar}, el mismo que usan los demás módulos.</p>
 */
public final class ClienteDialogo {

    private ClienteDialogo() {
    }

    /**
     * @param original cliente a editar, o {@code null} para crear uno
     * @return el cliente guardado, o vacío si el usuario canceló
     */
    public static Optional<Cliente> mostrar(Window ventana, ClienteService service, Cliente original) {
        ClienteFormController formulario = new ClienteFormController(original);
        Parent contenido = Vistas.cargarCon("view/clientes/cliente-form.fxml", formulario);
        return DialogoGuardar.mostrar(ventana,
                original == null ? "Nuevo cliente" : "Editar cliente",
                original == null ? "Registrar un cliente o prospecto" : original.nombre(),
                contenido, "Guardar",
                () -> {
                    ClienteFormulario datos = formulario.leerFormulario(); // se lee en el hilo de JavaFX
                    return () -> service.guardar(original, datos);
                },
                formulario::mostrarError,
                () -> {
                    formulario.activarValidaciones();
                    formulario.enfocarPrimerCampo();
                });
    }
}
