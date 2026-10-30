package com.devsuj.crmdesktop.view.campanas;

import com.devsuj.crmdesktop.model.Campana;
import com.devsuj.crmdesktop.model.Cliente;
import com.devsuj.crmdesktop.model.Destinatario;
import com.devsuj.crmdesktop.service.CampanaService;
import com.devsuj.crmdesktop.view.comun.DialogoGuardar;
import com.devsuj.crmdesktop.view.comun.Formulario;
import com.devsuj.crmdesktop.view.comun.Selectores;
import javafx.stage.Window;
import org.controlsfx.control.SearchableComboBox;

import java.util.List;
import java.util.Optional;

/** Agrega un cliente puntual a los destinatarios de una campaña. */
public final class AgregarDestinatarioDialogo {

    private AgregarDestinatarioDialogo() {
    }

    public static Optional<Destinatario> mostrar(Window ventana, CampanaService service, Campana campana,
                                                 List<Cliente> clientes) {
        Formulario formulario = new Formulario();
        SearchableComboBox<Cliente> cliente = formulario.campoAncho("Cliente *", "clienteId",
                Selectores.clientes(clientes));
        formulario.regla(cliente, true, () -> cliente.getValue() == null
                ? Optional.of("Elige el cliente.") : Optional.empty());
        return DialogoGuardar.mostrar(ventana, "Agregar destinatario", campana.nombre(), formulario, "Agregar",
                () -> {
                    Cliente elegido = cliente.getValue();
                    return () -> service.agregarDestinatario(campana, elegido);
                });
    }
}
