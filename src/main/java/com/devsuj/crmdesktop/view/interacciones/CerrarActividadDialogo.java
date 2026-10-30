package com.devsuj.crmdesktop.view.interacciones;

import com.devsuj.crmdesktop.model.EstadoInteraccion;
import com.devsuj.crmdesktop.model.Interaccion;
import com.devsuj.crmdesktop.service.InteraccionService;
import com.devsuj.crmdesktop.view.comun.DialogoGuardar;
import com.devsuj.crmdesktop.view.comun.Formulario;
import javafx.scene.control.TextArea;
import javafx.stage.Window;

import java.util.Optional;

/** Diálogo para cerrar una actividad PROGRAMADA: marcarla realizada (con resultado) o cancelarla. */
public final class CerrarActividadDialogo {

    private CerrarActividadDialogo() {
    }

    public static Optional<Interaccion> mostrar(Window ventana, InteraccionService service, Interaccion actividad,
                                                EstadoInteraccion nuevoEstado) {
        boolean realizada = nuevoEstado == EstadoInteraccion.REALIZADA;
        Formulario formulario = new Formulario();
        TextArea resultado = formulario.campoAncho(realizada ? "¿Qué resultado tuvo?" : "Motivo de la cancelación",
                InteraccionService.RESULTADO, new TextArea());
        resultado.setPrefRowCount(4);
        resultado.setWrapText(true);
        resultado.setPromptText(realizada ? "Por ejemplo: el cliente pidió una cotización formal" : "Opcional");

        return DialogoGuardar.mostrar(ventana,
                realizada ? "Marcar como realizada" : "Cancelar actividad",
                actividad.asunto() + " · " + actividad.clienteNombre(),
                formulario, realizada ? "Marcar realizada" : "Cancelar actividad",
                () -> {
                    String texto = resultado.getText();
                    return () -> service.cerrar(actividad, nuevoEstado, texto);
                });
    }
}
