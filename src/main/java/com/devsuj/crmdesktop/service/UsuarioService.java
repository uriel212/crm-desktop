package com.devsuj.crmdesktop.service;

import com.devsuj.crmdesktop.client.ApiException;
import com.devsuj.crmdesktop.client.UsuarioApi;
import com.devsuj.crmdesktop.model.Pagina;
import com.devsuj.crmdesktop.model.Rol;
import com.devsuj.crmdesktop.model.Usuario;
import com.devsuj.crmdesktop.model.UsuarioActualizarRequest;
import com.devsuj.crmdesktop.model.UsuarioCrearRequest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Lógica del módulo de usuarios y equipos: validar, crear/editar, activar/desactivar (con la
 * reasignación de clientes que pide la API) y cambiar contraseñas. Sin JavaFX.
 */
public class UsuarioService {

    public static final int TAMANO_PAGINA = 20;
    public static final String NOMBRE = "nombre";
    public static final String EMAIL = "email";
    public static final String PASSWORD = "password";
    public static final String ROL = "rol";
    public static final String GERENTE = "gerenteId";
    public static final String PASSWORD_ACTUAL = "passwordActual";
    public static final String PASSWORD_NUEVA = "passwordNueva";
    public static final String CONFIRMACION = "confirmacion";

    /** Propiedad del 409 que indica cuántos clientes tiene el vendedor que se quiere desactivar. */
    public static final String CLIENTES_ASIGNADOS = "clientesAsignados";

    private final UsuarioApi api;

    public UsuarioService(UsuarioApi api) {
        this.api = api;
    }

    public Pagina<Usuario> buscar(String texto, Rol rol, Boolean activo, int pagina) {
        return api.buscar(Reglas.limpiar(texto), rol, activo, pagina, TAMANO_PAGINA);
    }

    /** Usuarios activos que pueden ser responsables de clientes (VENDEDOR y GERENTE). */
    public List<Usuario> responsables() {
        return api.buscar(null, null, true, 0, 100).contenido().stream()
                .filter(u -> u.rol() != Rol.ADMIN)
                .toList();
    }

    /** Gerentes activos (para elegir el equipo de un vendedor). */
    public List<Usuario> gerentes() {
        return api.buscar(null, Rol.GERENTE, true, 0, 100).contenido();
    }

    // ---------- Reglas (las mismas de UsuarioCrearRequest/UsuarioActualizarRequest de la API) ----------

    public static Optional<String> reglaNombre(String valor) {
        return Reglas.obligatorio(valor, "Escribe el nombre del usuario.").or(() -> Reglas.maximo(valor, 100));
    }

    public static Optional<String> reglaEmail(String valor) {
        return Reglas.email(valor, "Escribe el email del usuario.");
    }

    public static Optional<String> reglaPassword(String valor) {
        if (valor == null || valor.isEmpty()) {
            return Optional.of("Escribe una contraseña.");
        }
        return valor.length() < 8 || valor.length() > 72
                ? Optional.of("La contraseña debe tener entre 8 y 72 caracteres.")
                : Optional.empty();
    }

    /**
     * @param original usuario a editar, o {@code null} para crear uno
     * @throws ValidacionException si los datos no cumplen las reglas
     */
    public Usuario guardar(Usuario original, UsuarioFormulario datos) {
        Map<String, String> errores = new LinkedHashMap<>();
        Reglas.revisar(errores, NOMBRE, () -> reglaNombre(datos.nombre()));
        Reglas.revisar(errores, EMAIL, () -> reglaEmail(datos.email()));
        if (original == null) {
            Reglas.revisar(errores, PASSWORD, () -> reglaPassword(datos.password()));
        }
        Reglas.revisar(errores, ROL, () -> Reglas.obligatorio(datos.rol(), "Elige el rol."));
        Reglas.exigir(errores);

        // Solo un VENDEDOR pertenece al equipo de un gerente.
        Integer gerenteId = datos.rol() == Rol.VENDEDOR && datos.gerente() != null ? datos.gerente().id() : null;
        String nombre = datos.nombre().strip();
        String email = datos.email().strip();
        return original == null
                ? api.crear(new UsuarioCrearRequest(nombre, email, datos.password(), datos.rol(), gerenteId))
                : api.actualizar(original.id(), new UsuarioActualizarRequest(nombre, email, datos.rol(), gerenteId));
    }

    /**
     * Activa o desactiva un usuario.
     *
     * @param reasignarClientes {@code true} si el usuario ya confirmó que los clientes pasen al gerente
     * @throws ApiException 409; si trae {@link #clientesAsignados(ApiException)}, hay que pedir confirmación
     */
    public Usuario cambiarEstado(Usuario usuario, boolean activo, boolean reasignarClientes) {
        return api.cambiarEstado(usuario.id(), activo, reasignarClientes);
    }

    /** Cuántos clientes impiden desactivar al vendedor (vacío si el error es por otra causa). */
    public static Optional<Integer> clientesAsignados(Throwable error) {
        if (error instanceof ApiException api && api.status() == 409) {
            return api.dato(CLIENTES_ASIGNADOS).flatMap(Reglas::entero);
        }
        return Optional.empty();
    }

    /**
     * @param passwordActual obligatoria si el usuario cambia SU propia contraseña; {@code null} si un ADMIN
     *                       cambia la de otro
     */
    public void cambiarPassword(Usuario usuario, boolean esPropia, String passwordActual,
                                String passwordNueva, String confirmacion) {
        Map<String, String> errores = new LinkedHashMap<>();
        if (esPropia) {
            Reglas.revisar(errores, PASSWORD_ACTUAL,
                    () -> Reglas.obligatorio(passwordActual, "Escribe tu contraseña actual."));
        }
        Reglas.revisar(errores, PASSWORD_NUEVA, () -> reglaPassword(passwordNueva));
        if (passwordNueva != null && !passwordNueva.equals(confirmacion)) {
            errores.putIfAbsent(CONFIRMACION, "Las contraseñas no coinciden.");
        }
        Reglas.exigir(errores);
        api.cambiarPassword(usuario.id(), esPropia ? passwordActual : null, passwordNueva);
    }
}
