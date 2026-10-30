package com.devsuj.crmdesktop.view.comun;

import com.devsuj.crmdesktop.client.ApiException;
import com.devsuj.crmdesktop.service.ValidacionException;
import com.devsuj.crmdesktop.util.MensajesError;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Constructor de formularios con el mismo aspecto en toda la app: rejilla de dos columnas en
 * porcentaje (se adapta al ancho), etiqueta arriba de cada campo, mensaje de error debajo y un
 * mensaje general arriba. Los diálogos de cada módulo solo declaran sus campos (DRY y O de SOLID).
 *
 * <p>Las reglas se marcan con ControlsFX ({@code ValidationSupport}): el campo con error se pinta en
 * rojo y el mensaje aparece al pasar el ratón. Los errores que devuelve la API (400 por campo) se
 * escriben debajo del campo con el mismo nombre que usa la API.</p>
 *
 * <p>El orden en que se agregan los campos es el orden del Tab.</p>
 */
public class Formulario {

    private final GridPane rejilla = new GridPane();
    private final Label mensajeGeneral = new Label();
    private final VBox raiz;
    private final Map<String, Label> errores = new LinkedHashMap<>();
    private final ValidationSupport validacion = new ValidationSupport();
    private final List<Runnable> registros = new ArrayList<>();
    private Control primerCampo;
    private int fila;
    private int columna;

    public Formulario() {
        rejilla.setHgap(16);
        rejilla.setVgap(12);
        ColumnConstraints mitad = new ColumnConstraints();
        mitad.setPercentWidth(50);
        ColumnConstraints otraMitad = new ColumnConstraints();
        otraMitad.setPercentWidth(50);
        rejilla.getColumnConstraints().setAll(mitad, otraMitad);

        mensajeGeneral.getStyleClass().add("mensaje-general");
        mensajeGeneral.setWrapText(true);
        mensajeGeneral.setMaxWidth(Double.MAX_VALUE);
        ocultarSiEstaVacia(mensajeGeneral);

        Label obligatorios = new Label("* Campos obligatorios");
        obligatorios.getStyleClass().addAll("text-muted", "text-small");
        raiz = new VBox(12, mensajeGeneral, rejilla, obligatorios);
        raiz.setPrefWidth(560);
    }

    /** Campo que ocupa media fila. */
    public <C extends Control> C campo(String etiqueta, String nombreCampo, C control) {
        return agregar(etiqueta, nombreCampo, control, 1);
    }

    /** Campo de media fila con otros nodos a su derecha (por ejemplo, un botón "Agregar"). */
    public <C extends Control> C campoCon(String etiqueta, String nombreCampo, C control, Node... aLaDerecha) {
        agregar(etiqueta, nombreCampo, control, 1);
        VBox caja = (VBox) control.getParent();
        int posicion = caja.getChildren().indexOf(control);
        caja.getChildren().remove(control);
        HBox fila = new HBox(8, control);
        fila.getChildren().addAll(aLaDerecha);
        fila.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(control, Priority.ALWAYS);
        caja.getChildren().add(posicion, fila);
        return control;
    }

    /** Campo que ocupa la fila completa (textos largos). */
    public <C extends Control> C campoAncho(String etiqueta, String nombreCampo, C control) {
        if (columna != 0) {
            fila++;
            columna = 0;
        }
        return agregar(etiqueta, nombreCampo, control, 2);
    }

    /** Texto de ayuda o aviso que ocupa toda una fila. */
    public void nota(Node nodo) {
        if (columna != 0) {
            fila++;
            columna = 0;
        }
        rejilla.add(nodo, 0, fila++, 2, 1);
    }

    /**
     * Regla de validación de un campo. Se evalúa con los valores actuales del formulario, así puede
     * depender de otros campos (por ejemplo, "la fecha es obligatoria si la actividad es programada").
     */
    public void regla(Control control, boolean obligatorio, Supplier<Optional<String>> regla) {
        registros.add(() -> {
            Validator<Object> validador = (Control c, Object valor) -> {
                Optional<String> error = regla.get();
                return ValidationResult.fromErrorIf(c, error.orElse(""), error.isPresent());
            };
            validacion.registerValidator(control, obligatorio, validador);
        });
    }

    /**
     * Activa las marcas de ControlsFX. Se llama cuando la ventana YA se muestra: si se hace antes,
     * {@code Dialog.showAndWait()} falla con "DialogPane is already inside a scene-graph". Las marcas
     * se ven a partir del primer intento de guardar, para no "regañar" un formulario vacío.
     */
    public void activarValidaciones() {
        validacion.setErrorDecorationEnabled(false);
        registros.forEach(Runnable::run);
        registros.clear();
        if (primerCampo != null) {
            primerCampo.requestFocus();
        }
    }

    /** Llamar al pulsar "Guardar", antes de leer los valores. */
    public void prepararEnvio() {
        validacion.setErrorDecorationEnabled(true);
        mensajeGeneral.setText("");
    }

    /** Muestra el error al guardar: junto a cada campo si viene por campo, y un mensaje general arriba. */
    public void mostrarError(Throwable error) {
        Map<String, String> porCampo = switch (error) {
            case ValidacionException e -> e.erroresPorCampo();
            case ApiException e -> e.erroresPorCampo();
            default -> Map.of();
        };
        porCampo.forEach((campo, mensaje) -> {
            Label etiqueta = errores.get(campo);
            if (etiqueta != null) {
                etiqueta.setText(mensaje);
            }
        });
        mensajeGeneral.setText(MensajesError.de(error));
    }

    /** Ancho preferido del formulario (por defecto 560). */
    public void ancho(double ancho) {
        raiz.setPrefWidth(ancho);
        raiz.setMinWidth(ancho);
    }

    public Parent raiz() {
        return raiz;
    }

    private <C extends Control> C agregar(String etiqueta, String nombreCampo, C control, int ancho) {
        if (columna + ancho > 2) {
            fila++;
            columna = 0;
        }
        Label titulo = new Label(etiqueta);
        titulo.setLabelFor(control);
        Label error = new Label();
        error.getStyleClass().add("error-campo");
        error.setWrapText(true);
        ocultarSiEstaVacia(error);
        errores.put(nombreCampo, error);
        control.setMaxWidth(Double.MAX_VALUE);
        limpiarErrorAlCambiar(control, error);

        VBox caja = new VBox(4, titulo, control, error);
        GridPane.setHgrow(caja, Priority.ALWAYS);
        rejilla.add(caja, columna, fila, ancho, 1);
        columna += ancho;
        if (columna >= 2) {
            fila++;
            columna = 0;
        }
        if (primerCampo == null) {
            primerCampo = control;
        }
        return control;
    }

    /** Al corregir un campo se borra el mensaje de error que tenía debajo. */
    private static void limpiarErrorAlCambiar(Control control, Label error) {
        if (control instanceof TextInputControl texto) {
            texto.textProperty().addListener((obs, antes, ahora) -> error.setText(""));
        } else if (control instanceof ComboBoxBase<?> combo) {
            combo.valueProperty().addListener((obs, antes, ahora) -> error.setText(""));
        }
    }

    private static void ocultarSiEstaVacia(Label etiqueta) {
        // Alto mínimo = el que necesita el texto: así un mensaje largo pasa a otra línea en vez de cortarse con "...".
        etiqueta.setMinHeight(Region.USE_PREF_SIZE);
        etiqueta.visibleProperty().bind(etiqueta.textProperty().isNotEmpty());
        etiqueta.managedProperty().bind(etiqueta.visibleProperty());
    }
}
