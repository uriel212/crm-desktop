package com.devsuj.crmdesktop.view.clientes;

import com.devsuj.crmdesktop.model.Cliente;
import com.devsuj.crmdesktop.model.Usuario;
import com.devsuj.crmdesktop.service.ClienteService;
import com.devsuj.crmdesktop.view.comun.DialogoGuardar;
import com.devsuj.crmdesktop.view.comun.EtiquetaListCell;
import com.devsuj.crmdesktop.view.comun.Formulario;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Window;

import java.util.List;
import java.util.Optional;

/**
 * Diálogo para asignar o reasignar el responsable de uno o varios clientes (solo GERENTE y ADMIN).
 * Con un solo cliente se puede dejar "Sin responsable".
 */
public final class AsignarVendedorDialogo {

    private AsignarVendedorDialogo() {
    }

    /**
     * @param responsables vendedores y gerentes activos (ya cargados de la API)
     * @return cuántos clientes cambiaron, o vacío si se canceló
     */
    public static Optional<Integer> mostrar(Window ventana, ClienteService service, List<Cliente> clientes,
                                            List<Usuario> responsables) {
        boolean uno = clientes.size() == 1;
        Formulario formulario = new Formulario();
        ComboBox<Usuario> combo = formulario.campoAncho("Nuevo responsable *", "vendedorId", new ComboBox<>());
        if (uno) {
            combo.getItems().add(null); // null = sin responsable
        }
        combo.getItems().addAll(responsables);
        EtiquetaListCell.aplicar(combo, "Sin responsable", Usuario::nombreConRol);
        if (uno) {
            Integer actual = clientes.getFirst().vendedorId();
            combo.setValue(responsables.stream().filter(u -> u.id().equals(actual)).findFirst().orElse(null));
        } else {
            formulario.regla(combo, true, () -> combo.getValue() == null
                    ? Optional.of("Elige quién atenderá a los clientes.") : Optional.empty());
        }
        Label nota = new Label(uno
                ? "Cliente: " + clientes.getFirst().nombre()
                : clientes.size() + " clientes seleccionados pasarán al mismo responsable.");
        nota.getStyleClass().add("text-muted");
        nota.setWrapText(true);
        formulario.nota(nota);

        return DialogoGuardar.mostrar(ventana, "Asignar responsable",
                uno ? "Asignar responsable del cliente" : "Reasignar clientes en lote",
                formulario, "Asignar",
                () -> {
                    Usuario elegido = combo.getValue();
                    return () -> service.asignarVendedor(clientes, elegido);
                });
    }
}
