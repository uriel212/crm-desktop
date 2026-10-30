package com.devsuj.crmdesktop.view.oportunidades;

import com.devsuj.crmdesktop.model.EtapaOportunidad;
import com.devsuj.crmdesktop.model.Oportunidad;
import com.devsuj.crmdesktop.service.OportunidadService;
import com.devsuj.crmdesktop.view.comun.DialogoGuardar;
import com.devsuj.crmdesktop.view.comun.EtiquetaListCell;
import com.devsuj.crmdesktop.view.comun.Formulario;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Window;

import java.util.Optional;

/** Diálogo para mover una oportunidad abierta a otra etapa o marcarla como perdida. */
public final class CambioEtapaDialogo {

    private CambioEtapaDialogo() {
    }

    public static Optional<Oportunidad> mostrar(Window ventana, OportunidadService service, Oportunidad oportunidad) {
        Formulario formulario = new Formulario();
        ComboBox<EtapaOportunidad> etapa = formulario.campoAncho("Nueva etapa *", OportunidadService.ETAPA,
                new ComboBox<>());
        etapa.getItems().setAll(oportunidad.etapa().destinosPermitidos());
        EtiquetaListCell.aplicar(etapa, "Elige la etapa");
        TextArea comentario = formulario.campoAncho("Comentario", OportunidadService.COMENTARIO, new TextArea());
        comentario.setPrefRowCount(3);
        comentario.setWrapText(true);
        comentario.setPromptText("Qué pasó (obligatorio si se perdió). Máximo 255 caracteres.");
        formulario.regla(etapa, true, () -> etapa.getValue() == null
                ? Optional.of("Elige la nueva etapa.") : Optional.empty());

        Label nota = new Label("Para marcarla como Ganada, registra la venta: la API la cierra sola. "
                + "Una oportunidad perdida ya no se reabre (podrás crear un nuevo intento).");
        nota.getStyleClass().addAll("text-muted", "text-small");
        nota.setWrapText(true);
        formulario.nota(nota);

        return DialogoGuardar.mostrar(ventana, "Cambiar etapa",
                oportunidad.titulo() + " · ahora en " + oportunidad.etapa().etiqueta(),
                formulario, "Cambiar etapa",
                () -> {
                    EtapaOportunidad destino = etapa.getValue();
                    String texto = comentario.getText();
                    return () -> service.cambiarEtapa(oportunidad, destino, texto);
                });
    }
}
