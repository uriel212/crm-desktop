package com.devsuj.crmdesktop.view.comun;

import com.devsuj.crmdesktop.model.ConEtiqueta;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;

import java.util.function.Function;

/**
 * Celda para ComboBox y ListView que muestra un texto amigable. El valor {@code null} se muestra
 * con un texto propio (por ejemplo "Todas las etapas" en un filtro).
 */
public class EtiquetaListCell<T> extends ListCell<T> {

    private final String textoSinValor;
    private final Function<T, String> texto;

    public EtiquetaListCell(String textoSinValor, Function<T, String> texto) {
        this.textoSinValor = textoSinValor;
        this.texto = texto;
    }

    @Override
    protected void updateItem(T valor, boolean vacia) {
        super.updateItem(valor, vacia);
        setText(vacia ? null : valor == null ? textoSinValor : texto.apply(valor));
    }

    /** Configura las celdas del combo (la lista y el botón) con el mismo texto. */
    public static <T> void aplicar(ComboBox<T> combo, String textoSinValor, Function<T, String> texto) {
        combo.setCellFactory(lista -> new EtiquetaListCell<>(textoSinValor, texto));
        combo.setButtonCell(new EtiquetaListCell<>(textoSinValor, texto));
    }

    /** Atajo para enums con etiqueta. */
    public static <T extends ConEtiqueta> void aplicar(ComboBox<T> combo, String textoSinValor) {
        aplicar(combo, textoSinValor, ConEtiqueta::etiqueta);
    }

    /** Llena un combo de filtro: primero {@code null} ("Todos...") y luego los valores. */
    @SafeVarargs
    public static <T extends ConEtiqueta> void filtro(ComboBox<T> combo, String textoTodos, T... valores) {
        combo.getItems().clear();
        combo.getItems().add(null);
        combo.getItems().addAll(valores);
        aplicar(combo, textoTodos);
    }
}
