package com.devsuj.crmdesktop.view.comun;

import com.devsuj.crmdesktop.model.Cliente;
import com.devsuj.crmdesktop.model.Producto;
import com.devsuj.crmdesktop.util.Formatos;
import javafx.util.StringConverter;
import org.controlsfx.control.SearchableComboBox;

import java.util.List;
import java.util.function.Function;

/**
 * Combos de búsqueda reutilizables (ControlsFX {@link SearchableComboBox}): se escribe para filtrar
 * la lista, útil cuando hay muchos clientes o productos.
 */
public final class Selectores {

    private Selectores() {
    }

    /** Combo de clientes que filtra por nombre o empresa. */
    public static SearchableComboBox<Cliente> clientes(List<Cliente> clientes) {
        return buscable(clientes, "Escribe para buscar un cliente", c ->
                c.empresa() == null || c.empresa().isBlank() ? c.nombre() : c.nombre() + " · " + c.empresa());
    }

    /** Combo de productos con precio y existencias. */
    public static SearchableComboBox<Producto> productos(List<Producto> productos) {
        return buscable(productos, "Escribe para buscar un producto", p ->
                p.nombre() + " · " + Formatos.moneda(p.precio()) + " · " + p.stock() + " disp.");
    }

    public static <T> SearchableComboBox<T> buscable(List<T> elementos, String ayuda, Function<T, String> texto) {
        SearchableComboBox<T> combo = new SearchableComboBox<>();
        combo.getItems().setAll(elementos);
        combo.setPromptText(ayuda);
        combo.setConverter(new StringConverter<>() {
            @Override
            public String toString(T valor) {
                return valor == null ? "" : texto.apply(valor);
            }

            @Override
            public T fromString(String escrito) {
                return null; // no se crean registros escribiendo en el combo
            }
        });
        return combo;
    }
}
