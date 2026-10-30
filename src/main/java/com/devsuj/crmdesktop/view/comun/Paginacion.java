package com.devsuj.crmdesktop.view.comun;

import com.devsuj.crmdesktop.model.Pagina;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.util.function.IntConsumer;

/**
 * Controles de paginación (anterior, siguiente y "Página 1 de 3 · 57 clientes") conectados con la
 * paginación de la API ({@code ?page=}). Lo usan todos los listados (DRY).
 */
public class Paginacion {

    private final IntegerProperty pagina = new SimpleIntegerProperty(0);
    private final IntegerProperty totalPaginas = new SimpleIntegerProperty(0);
    private final Label etiqueta;
    private final String nombrePlural;

    /**
     * @param cargando     mientras vale {@code true}, los botones se deshabilitan
     * @param irAPagina    qué hacer al pedir otra página (normalmente, recargar el listado)
     * @param nombrePlural por ejemplo "clientes"
     */
    public Paginacion(Button anterior, Button siguiente, Label etiqueta, BooleanProperty cargando,
                      IntConsumer irAPagina, String nombrePlural) {
        this.etiqueta = etiqueta;
        this.nombrePlural = nombrePlural;
        anterior.disableProperty().bind(pagina.lessThanOrEqualTo(0).or(cargando));
        siguiente.disableProperty().bind(pagina.add(1).greaterThanOrEqualTo(totalPaginas).or(cargando));
        anterior.setOnAction(evento -> irAPagina.accept(pagina.get() - 1));
        siguiente.setOnAction(evento -> irAPagina.accept(pagina.get() + 1));
    }

    public void mostrar(Pagina<?> resultado) {
        pagina.set(resultado.pagina());
        totalPaginas.set(resultado.totalPaginas());
        etiqueta.setText(resultado.estaVacia() ? "" : "Página %d de %d · %d %s".formatted(
                resultado.pagina() + 1, resultado.totalPaginas(), resultado.totalElementos(), nombrePlural));
    }

    public void limpiar() {
        totalPaginas.set(0);
        etiqueta.setText("");
    }

    /** Página actual (empieza en 0). */
    public int actual() {
        return pagina.get();
    }
}
