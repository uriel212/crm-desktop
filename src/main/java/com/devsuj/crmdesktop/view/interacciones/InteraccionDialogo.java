package com.devsuj.crmdesktop.view.interacciones;

import com.devsuj.crmdesktop.model.Cliente;
import com.devsuj.crmdesktop.model.EstadoInteraccion;
import com.devsuj.crmdesktop.model.Interaccion;
import com.devsuj.crmdesktop.model.TipoInteraccion;
import com.devsuj.crmdesktop.service.InteraccionFormulario;
import com.devsuj.crmdesktop.service.InteraccionService;
import com.devsuj.crmdesktop.util.Formatos;
import com.devsuj.crmdesktop.view.comun.DialogoGuardar;
import com.devsuj.crmdesktop.view.comun.EtiquetaListCell;
import com.devsuj.crmdesktop.view.comun.Formulario;
import com.devsuj.crmdesktop.view.comun.Selectores;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Window;
import org.controlsfx.control.SearchableComboBox;

import java.util.List;
import java.util.Optional;

/**
 * Diálogo para registrar un contacto (ya ocurrió), agendar una actividad futura, o corregir y
 * reprogramar una existente.
 */
public final class InteraccionDialogo {

    private InteraccionDialogo() {
    }

    /**
     * @param original interacción a editar, o {@code null} para crear
     * @param clientes clientes que el usuario puede gestionar (solo al crear)
     */
    public static Optional<Interaccion> mostrar(Window ventana, InteraccionService service, Interaccion original,
                                                List<Cliente> clientes) {
        Formulario formulario = new Formulario();
        SearchableComboBox<Cliente> cliente = Selectores.clientes(clientes);
        ComboBox<EstadoInteraccion> estado = new ComboBox<>();
        if (original == null) {
            formulario.campoAncho("Cliente *", InteraccionService.CLIENTE, cliente);
            formulario.regla(cliente, true, () -> cliente.getValue() == null
                    ? Optional.of("Elige el cliente.") : Optional.empty());
            formulario.campo("¿Ya ocurrió? *", InteraccionService.ESTADO, estado);
            estado.getItems().setAll(EstadoInteraccion.REALIZADA, EstadoInteraccion.PROGRAMADA);
            EtiquetaListCell.aplicar(estado, "", e -> e == EstadoInteraccion.REALIZADA
                    ? "Sí, ya ocurrió (Realizada)" : "No, es a futuro (Programada)");
            estado.setValue(EstadoInteraccion.REALIZADA);
        }
        ComboBox<TipoInteraccion> tipo = formulario.campo("Tipo *", InteraccionService.TIPO, new ComboBox<>());
        tipo.getItems().setAll(TipoInteraccion.values());
        EtiquetaListCell.aplicar(tipo, "Elige el tipo");
        DatePicker fecha = formulario.campo("Fecha", InteraccionService.FECHA, new DatePicker());
        TextField hora = formulario.campo("Hora (24 h)", InteraccionService.HORA, new TextField());
        hora.setPromptText("09:30");
        TextField asunto = formulario.campoAncho("Asunto *", InteraccionService.ASUNTO, new TextField());
        TextArea descripcion = formulario.campoAncho("Descripción", InteraccionService.DESCRIPCION, new TextArea());
        descripcion.setPrefRowCount(4);
        descripcion.setWrapText(true);
        formulario.regla(asunto, true, () -> InteraccionService.reglaAsunto(asunto.getText()));
        formulario.regla(hora, false, () -> hora.getText().isBlank() || InteraccionService.hora(hora.getText()).isPresent()
                ? Optional.empty() : Optional.of("Escribe la hora como 09:30."));

        if (original != null) {
            tipo.setValue(original.tipo());
            fecha.setValue(original.fecha() == null ? null : original.fecha().toLocalDate());
            hora.setText(Formatos.hora(original.fecha()));
            asunto.setText(original.asunto());
            descripcion.setText(original.descripcion());
        } else {
            tipo.setValue(TipoInteraccion.LLAMADA);
            fecha.setValue(Formatos.hoy());
            // Al agendar a futuro se sugiere mañana a las 09:00.
            estado.valueProperty().addListener((obs, antes, ahora) -> {
                if (ahora == EstadoInteraccion.PROGRAMADA) {
                    fecha.setValue(Formatos.hoy().plusDays(1));
                    hora.setText("09:00");
                } else {
                    fecha.setValue(Formatos.hoy());
                    hora.clear();
                }
            });
        }

        String titulo = original == null ? "Nueva interacción"
                : original.estado() == EstadoInteraccion.PROGRAMADA ? "Reprogramar actividad" : "Corregir interacción";
        return DialogoGuardar.mostrar(ventana, titulo,
                original == null ? "Registra un contacto o agenda una actividad" : original.clienteNombre(),
                formulario, "Guardar",
                () -> {
                    var datos = new InteraccionFormulario(cliente.getValue(), tipo.getValue(), estado.getValue(),
                            fecha.getValue(), hora.getText(), asunto.getText(), descripcion.getText());
                    return () -> service.guardar(original, datos);
                });
    }
}
