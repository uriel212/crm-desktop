package com.devsuj.crmdesktop.view.campanas;

import com.devsuj.crmdesktop.model.CanalCampana;
import com.devsuj.crmdesktop.model.Campana;
import com.devsuj.crmdesktop.model.SegmentoCampana;
import com.devsuj.crmdesktop.service.CampanaFormulario;
import com.devsuj.crmdesktop.service.CampanaService;
import com.devsuj.crmdesktop.util.Formatos;
import com.devsuj.crmdesktop.view.comun.DialogoGuardar;
import com.devsuj.crmdesktop.view.comun.EtiquetaListCell;
import com.devsuj.crmdesktop.view.comun.Formulario;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Window;

import java.util.Optional;

/** Diálogo para crear o editar una campaña (GERENTE y ADMIN). Nace PLANEADA. */
public final class CampanaDialogo {

    private CampanaDialogo() {
    }

    public static Optional<Campana> mostrar(Window ventana, CampanaService service, Campana original) {
        Formulario formulario = new Formulario();
        TextField nombre = formulario.campoAncho("Nombre *", CampanaService.NOMBRE, new TextField());
        ComboBox<CanalCampana> canal = formulario.campo("Canal *", CampanaService.CANAL, new ComboBox<>());
        canal.getItems().setAll(CanalCampana.values());
        EtiquetaListCell.aplicar(canal, "Elige el canal");
        ComboBox<SegmentoCampana> segmento = formulario.campo("Dirigida a *", CampanaService.SEGMENTO, new ComboBox<>());
        segmento.getItems().setAll(SegmentoCampana.values());
        EtiquetaListCell.aplicar(segmento, "Elige el segmento");
        DatePicker inicio = formulario.campo("Inicio *", CampanaService.FECHA_INICIO, new DatePicker());
        DatePicker fin = formulario.campo("Fin *", CampanaService.FECHA_FIN, new DatePicker());
        TextField presupuesto = formulario.campo("Presupuesto (Q) *", CampanaService.PRESUPUESTO, new TextField());
        presupuesto.setPromptText("5000.00");
        TextField asunto = formulario.campoAncho("Asunto *", CampanaService.ASUNTO, new TextField());
        TextArea mensaje = formulario.campoAncho("Mensaje *", CampanaService.MENSAJE, new TextArea());
        mensaje.setPrefRowCount(4);
        mensaje.setWrapText(true);
        formulario.regla(nombre, true, () -> nombre.getText().isBlank()
                ? Optional.of("Escribe el nombre de la campaña.") : Optional.empty());
        formulario.regla(asunto, true, () -> asunto.getText().isBlank()
                ? Optional.of("Escribe el asunto.") : Optional.empty());

        if (original != null) {
            nombre.setText(original.nombre());
            canal.setValue(original.canal());
            segmento.setValue(original.segmento());
            inicio.setValue(original.fechaInicio());
            fin.setValue(original.fechaFin());
            presupuesto.setText(original.presupuesto() == null ? "" : original.presupuesto().toPlainString());
            asunto.setText(original.asunto());
            mensaje.setText(original.mensaje());
        } else {
            canal.setValue(CanalCampana.CORREO);
            segmento.setValue(SegmentoCampana.TODOS);
            inicio.setValue(Formatos.hoy());
            fin.setValue(Formatos.hoy().plusMonths(1));
        }

        return DialogoGuardar.mostrar(ventana, original == null ? "Nueva campaña" : "Editar campaña",
                original == null ? "La campaña nace como planeada" : original.nombre(),
                formulario, "Guardar",
                () -> {
                    var datos = new CampanaFormulario(nombre.getText(), canal.getValue(), asunto.getText(),
                            mensaje.getText(), segmento.getValue(), inicio.getValue(), fin.getValue(),
                            presupuesto.getText());
                    return () -> service.guardar(original, datos);
                });
    }
}
