package com.devsuj.crmdesktop.view.campanas;

import com.devsuj.crmdesktop.model.Campana;
import com.devsuj.crmdesktop.model.EstadoCampana;
import com.devsuj.crmdesktop.service.CampanaService;
import com.devsuj.crmdesktop.view.comun.DialogoGuardar;
import com.devsuj.crmdesktop.view.comun.EtiquetaListCell;
import com.devsuj.crmdesktop.view.comun.Formulario;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Window;

import java.util.Arrays;
import java.util.Optional;

/** Cambio manual del estado de una campaña (la API permite corregir errores en cualquier sentido). */
public final class EstadoCampanaDialogo {

    private EstadoCampanaDialogo() {
    }

    public static Optional<Campana> mostrar(Window ventana, CampanaService service, Campana campana) {
        Formulario formulario = new Formulario();
        ComboBox<EstadoCampana> estado = formulario.campoAncho("Nuevo estado *", "estado", new ComboBox<>());
        estado.getItems().setAll(Arrays.stream(EstadoCampana.values()).filter(e -> e != campana.estado()).toList());
        EtiquetaListCell.aplicar(estado, "Elige el estado");
        Label nota = new Label("Activa: las promociones ligadas aplican y se registran resultados. "
                + "Finalizada o cancelada: ya no se edita ni se agregan destinatarios.");
        nota.getStyleClass().addAll("text-muted", "text-small");
        nota.setWrapText(true);
        formulario.nota(nota);

        return DialogoGuardar.mostrar(ventana, "Cambiar estado",
                campana.nombre() + " · ahora " + campana.estado().etiqueta(), formulario, "Cambiar estado",
                () -> {
                    EstadoCampana elegido = estado.getValue();
                    return () -> service.cambiarEstado(campana, elegido);
                });
    }
}
