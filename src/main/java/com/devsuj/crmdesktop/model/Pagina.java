package com.devsuj.crmdesktop.model;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.util.List;

/**
 * Una página de resultados ({@code PaginaResponse<T>} de la API, plan del backend 0.12).
 * Es genérica: sirve para clientes, ventas, productos, etc. (DRY).
 *
 * @param pagina número de página, empieza en 0
 * @param tamano cuántos elementos caben en cada página
 */
public record Pagina<T>(
        List<T> contenido,
        int pagina,
        @JsonAlias("tamaño") int tamano,
        long totalElementos,
        int totalPaginas) {

    public Pagina {
        contenido = contenido == null ? List.of() : List.copyOf(contenido);
    }

    public boolean estaVacia() {
        return contenido.isEmpty();
    }
}
