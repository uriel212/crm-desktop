package com.devsuj.crmdesktop.view.comun;

import com.devsuj.crmdesktop.model.ConEtiqueta;
import com.devsuj.crmdesktop.util.Formatos;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.function.Function;

/**
 * Configuración común de tablas y columnas para todos los módulos (DRY): texto con guion si está
 * vacío, moneda en quetzales alineada a la derecha, fechas y etiquetas de color.
 *
 * <p>Los records no tienen "properties" de JavaFX; aquí se envuelve cada valor para la columna.</p>
 */
public final class Tablas {

    private Tablas() {
    }

    /** Ajusta las columnas al ancho disponible (la tabla crece con la ventana). */
    public static void ajustable(TableView<?> tabla) {
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

    public static <S> void texto(TableColumn<S, String> columna, Function<S, String> valor) {
        columna.setCellValueFactory(fila ->
                new ReadOnlyStringWrapper(Formatos.textoOGuion(valor.apply(fila.getValue()))));
    }

    public static <S> void moneda(TableColumn<S, String> columna, Function<S, BigDecimal> valor) {
        columna.setCellValueFactory(fila -> new ReadOnlyStringWrapper(Formatos.moneda(valor.apply(fila.getValue()))));
        alinearDerecha(columna);
    }

    public static <S> void numero(TableColumn<S, String> columna, Function<S, Object> valor) {
        columna.setCellValueFactory(fila -> {
            Object numero = valor.apply(fila.getValue());
            return new ReadOnlyStringWrapper(numero == null ? "—" : Formatos.entero(numero));
        });
        alinearDerecha(columna);
    }

    public static <S> void fecha(TableColumn<S, String> columna, Function<S, LocalDate> valor) {
        columna.setCellValueFactory(fila -> new ReadOnlyStringWrapper(
                Formatos.textoOGuion(Formatos.fecha(valor.apply(fila.getValue())))));
    }

    public static <S> void fechaHora(TableColumn<S, String> columna, Function<S, LocalDateTime> valor) {
        columna.setCellValueFactory(fila -> new ReadOnlyStringWrapper(
                Formatos.textoOGuion(Formatos.fechaHora(valor.apply(fila.getValue())))));
    }

    /** Columna de enum mostrada como etiqueta de color (ver {@link EtiquetaCell}). */
    public static <S, T extends Enum<T> & ConEtiqueta> void etiqueta(TableColumn<S, T> columna,
                                                                   Function<S, T> valor, String prefijoCss) {
        columna.setCellValueFactory(fila -> new ReadOnlyObjectWrapper<>(valor.apply(fila.getValue())));
        columna.setCellFactory(c -> new EtiquetaCell<>(prefijoCss));
    }

    /** Doble clic en una fila con datos = acción (por ejemplo, editar o ver detalle). */
    public static <S> void alDobleClic(TableView<S> tabla, Runnable accion) {
        filas(tabla).accion = accion;
    }

    /** Texto que aparece al pasar el ratón por una fila (por ejemplo, la descripción completa). */
    public static <S> void tooltipDeFila(TableView<S> tabla, Function<S, String> texto) {
        filas(tabla).tooltip = texto;
    }

    /** Fábrica de filas compartida por las dos opciones anteriores (se pueden usar juntas). */
    private static final class Filas<S> {
        private Runnable accion;
        private Function<S, String> tooltip;
    }

    @SuppressWarnings("unchecked")
    private static <S> Filas<S> filas(TableView<S> tabla) {
        Object existente = tabla.getProperties().get(Filas.class);
        if (existente != null) {
            return (Filas<S>) existente;
        }
        Filas<S> config = new Filas<>();
        tabla.getProperties().put(Filas.class, config);
        tabla.setRowFactory(t -> {
            TableRow<S> fila = new TableRow<>() {
                @Override
                protected void updateItem(S item, boolean vacia) {
                    super.updateItem(item, vacia);
                    String texto = vacia || item == null || config.tooltip == null ? null : config.tooltip.apply(item);
                    setTooltip(texto == null ? null : new Tooltip(texto));
                }
            };
            fila.setOnMouseClicked(evento -> {
                if (config.accion == null) {
                    return;
                }
                if (evento.getButton() == MouseButton.PRIMARY && evento.getClickCount() == 2 && !fila.isEmpty()) {
                    config.accion.run();
                }
            });
            return fila;
        });
        return config;
    }

    private static <S> void alinearDerecha(TableColumn<S, String> columna) {
        columna.setCellFactory(c -> {
            TableCell<S, String> celda = new TableCell<>() {
                @Override
                protected void updateItem(String texto, boolean vacia) {
                    super.updateItem(texto, vacia);
                    setText(vacia ? null : texto);
                }
            };
            celda.setAlignment(Pos.CENTER_RIGHT);
            return celda;
        });
    }
}
