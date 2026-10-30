package com.devsuj.crmdesktop.view;

import com.devsuj.crmdesktop.service.Permisos;

import java.util.function.Predicate;

/**
 * Una opción del menú lateral.
 *
 * @param nombre    texto del botón
 * @param icono     literal de Ikonli Feather (https://kordamp.org/ikonli/cheat-sheet-feather.html)
 * @param rutaFxml  pantalla a mostrar; {@code null} si el módulo aún no está implementado
 * @param visibleSi qué usuarios ven el módulo en el menú (por ejemplo, solo GERENTE y ADMIN)
 */
public record Modulo(String nombre, String icono, String rutaFxml, Predicate<Permisos> visibleSi) {

    /** Módulo visible para todos los roles. */
    public Modulo(String nombre, String icono, String rutaFxml) {
        this(nombre, icono, rutaFxml, permisos -> true);
    }

    public boolean disponible() {
        return rutaFxml != null;
    }
}
