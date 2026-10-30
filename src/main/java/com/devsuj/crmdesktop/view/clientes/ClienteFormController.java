package com.devsuj.crmdesktop.view.clientes;

import com.devsuj.crmdesktop.client.ApiException;
import com.devsuj.crmdesktop.model.Cliente;
import com.devsuj.crmdesktop.model.EstadoCliente;
import com.devsuj.crmdesktop.service.ClienteFormulario;
import com.devsuj.crmdesktop.service.ValidacionException;
import com.devsuj.crmdesktop.service.ValidadorCliente;
import com.devsuj.crmdesktop.util.MensajesError;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Controlador del formulario de cliente ({@code cliente-form.fxml}). Solo maneja la vista:
 * llena los campos, marca errores y entrega lo escrito como {@link ClienteFormulario}.
 * Guardar lo coordina {@link ClienteDialogo}.
 */
public class ClienteFormController {

    private final Cliente original;
    private final ValidationSupport validacion = new ValidationSupport();
    private Map<String, Label> erroresPorCampo;

    @FXML
    private TextField campoNombre;
    @FXML
    private TextField campoEmpresa;
    @FXML
    private TextField campoEmail;
    @FXML
    private TextField campoTelefono;
    @FXML
    private TextField campoDireccion;
    @FXML
    private ComboBox<EstadoCliente> comboEstado;
    @FXML
    private Label errorNombre;
    @FXML
    private Label errorEmpresa;
    @FXML
    private Label errorEmail;
    @FXML
    private Label errorTelefono;
    @FXML
    private Label errorDireccion;
    @FXML
    private Label mensajeGeneral;

    /** @param original cliente a editar, o {@code null} para uno nuevo */
    public ClienteFormController(Cliente original) {
        this.original = original;
    }

    @FXML
    private void initialize() {
        erroresPorCampo = Map.of(
                ValidadorCliente.NOMBRE, errorNombre,
                ValidadorCliente.EMPRESA, errorEmpresa,
                ValidadorCliente.EMAIL, errorEmail,
                ValidadorCliente.TELEFONO, errorTelefono,
                ValidadorCliente.DIRECCION, errorDireccion);
        // Las etiquetas de error solo ocupan espacio cuando tienen texto.
        erroresPorCampo.values().forEach(this::ocultarSiEstaVacia);
        ocultarSiEstaVacia(mensajeGeneral);

        comboEstado.getItems().setAll(EstadoCliente.values());
        comboEstado.setCellFactory(lista -> new EstadoClienteListCell(""));
        comboEstado.setButtonCell(new EstadoClienteListCell(""));

        if (original != null) {
            campoNombre.setText(original.nombre());
            campoEmpresa.setText(original.empresa());
            campoEmail.setText(original.email());
            campoTelefono.setText(original.telefono());
            campoDireccion.setText(original.direccion());
            comboEstado.setValue(original.estado());
        } else {
            comboEstado.setValue(EstadoCliente.PROSPECTO); // la API también usa PROSPECTO por defecto
        }
    }

    /**
     * ControlsFX marca en rojo el campo con error y muestra el mensaje al pasar el ratón.
     * Las reglas son las de {@link ValidadorCliente} (las mismas que usa el service).
     * Las marcas se activan al primer intento de guardar, para no "regañar" un formulario vacío.
     *
     * <p>Se llama cuando la ventana YA se está mostrando: ControlsFX envuelve la raíz de la escena
     * para dibujar las marcas, y si lo hace antes de {@code Dialog.showAndWait()} el diálogo falla con
     * "DialogPane is already inside a scene-graph".</p>
     */
    public void activarValidaciones() {
        validacion.setErrorDecorationEnabled(false);
        registrar(campoNombre, true, ValidadorCliente::nombre, ValidadorCliente.NOMBRE);
        registrar(campoEmpresa, false, ValidadorCliente::empresa, ValidadorCliente.EMPRESA);
        registrar(campoEmail, true, ValidadorCliente::email, ValidadorCliente.EMAIL);
        registrar(campoTelefono, false, ValidadorCliente::telefono, ValidadorCliente.TELEFONO);
        registrar(campoDireccion, false, ValidadorCliente::direccion, ValidadorCliente.DIRECCION);
    }

    private void registrar(TextField campo, boolean obligatorio, Function<String, Optional<String>> regla,
                           String nombreCampo) {
        Validator<String> validador = (Control control, String valor) -> {
            Optional<String> error = regla.apply(valor);
            return ValidationResult.fromErrorIf(control, error.orElse(""), error.isPresent());
        };
        validacion.registerValidator(campo, obligatorio, validador);
        // Al corregir un campo se borra el mensaje de error que tenía debajo.
        campo.textProperty().addListener((obs, antes, ahora) -> erroresPorCampo.get(nombreCampo).setText(""));
    }

    public void enfocarPrimerCampo() {
        campoNombre.requestFocus();
    }

    public ClienteFormulario leerFormulario() {
        validacion.setErrorDecorationEnabled(true);
        mensajeGeneral.setText("");
        return new ClienteFormulario(campoNombre.getText(), campoEmpresa.getText(), campoEmail.getText(),
                campoTelefono.getText(), campoDireccion.getText(), comboEstado.getValue());
    }

    /** Muestra el error al guardar: junto a cada campo si viene por campo, y un mensaje general arriba. */
    public void mostrarError(Throwable error) {
        Map<String, String> porCampo = switch (error) {
            case ValidacionException e -> e.erroresPorCampo();
            case ApiException e -> e.erroresPorCampo();
            default -> Map.of();
        };
        porCampo.forEach((campo, mensaje) -> {
            Label etiqueta = erroresPorCampo.get(campo);
            if (etiqueta != null) {
                etiqueta.setText(mensaje);
            }
        });
        mensajeGeneral.setText(MensajesError.de(error));
    }

    private void ocultarSiEstaVacia(Label etiqueta) {
        etiqueta.visibleProperty().bind(etiqueta.textProperty().isNotEmpty());
        etiqueta.managedProperty().bind(etiqueta.visibleProperty());
    }
}
