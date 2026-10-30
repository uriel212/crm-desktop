package com.devsuj.crmdesktop.client;

import com.devsuj.crmdesktop.model.Pagina;
import com.devsuj.crmdesktop.model.Rol;
import com.devsuj.crmdesktop.model.Usuario;
import com.devsuj.crmdesktop.model.UsuarioActualizarRequest;
import com.devsuj.crmdesktop.model.UsuarioCrearRequest;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * {@link UsuarioApi} en memoria (pruebas y modo demostración). Imita las reglas de la API:
 * email único (409) y 409 con {@code clientesAsignados} al desactivar un vendedor con clientes.
 */
public class UsuarioApiEnMemoria implements UsuarioApi {

    private final Map<Integer, Usuario> usuarios = new LinkedHashMap<>();
    private final Map<Integer, Integer> clientesPorUsuario = new HashMap<>();
    private int siguienteId = 1;

    /** Los usuarios del seed de la base de datos. */
    public static UsuarioApiEnMemoria conDatosDeEjemplo() {
        UsuarioApiEnMemoria api = new UsuarioApiEnMemoria();
        api.agregar("Ana Torres", "ana.torres@crmventas.test", Rol.ADMIN, null, true, 0);
        api.agregar("Luis Ramírez", "luis.ramirez@crmventas.test", Rol.GERENTE, null, true, 0);
        api.agregar("Carla Mendoza", "carla.mendoza@crmventas.test", Rol.VENDEDOR, 2, true, 5);
        api.agregar("Jorge Pérez", "jorge.perez@crmventas.test", Rol.VENDEDOR, 2, true, 4);
        api.agregar("Sofía Herrera", "sofia.herrera@crmventas.test", Rol.VENDEDOR, 2, true, 4);
        api.agregar("Mario Ruiz", "mario.ruiz@crmventas.test", Rol.VENDEDOR, 2, false, 1);
        return api;
    }

    @Override
    public synchronized Pagina<Usuario> buscar(String texto, Rol rol, Boolean activo, int pagina, int tamano) {
        String filtro = texto == null ? "" : texto.strip().toLowerCase(Locale.ROOT);
        List<Usuario> encontrados = usuarios.values().stream()
                .filter(u -> rol == null || u.rol() == rol)
                .filter(u -> activo == null || u.activo() == activo)
                .filter(u -> filtro.isEmpty() || u.nombre().toLowerCase(Locale.ROOT).contains(filtro)
                        || u.email().contains(filtro))
                .sorted(Comparator.comparing(Usuario::nombre))
                .toList();
        int desde = Math.min(pagina * tamano, encontrados.size());
        int hasta = Math.min(desde + tamano, encontrados.size());
        int totalPaginas = (int) Math.ceil(encontrados.size() / (double) tamano);
        return new Pagina<>(encontrados.subList(desde, hasta), pagina, tamano, encontrados.size(), totalPaginas);
    }

    @Override
    public synchronized Usuario crear(UsuarioCrearRequest datos) {
        validarEmailUnico(datos.email(), null);
        Usuario nuevo = new Usuario(siguienteId++, datos.nombre(), datos.email(), datos.rol(),
                datos.gerenteId(), nombreDe(datos.gerenteId()), true, LocalDateTime.now());
        usuarios.put(nuevo.id(), nuevo);
        return nuevo;
    }

    @Override
    public synchronized Usuario actualizar(int id, UsuarioActualizarRequest datos) {
        Usuario actual = obtener(id);
        validarEmailUnico(datos.email(), id);
        Usuario actualizado = new Usuario(id, datos.nombre(), datos.email(), datos.rol(), datos.gerenteId(),
                nombreDe(datos.gerenteId()), actual.activo(), actual.creadoEn());
        usuarios.put(id, actualizado);
        return actualizado;
    }

    @Override
    public synchronized Usuario cambiarEstado(int id, boolean activo, boolean reasignarClientes) {
        Usuario u = obtener(id);
        int clientes = clientesPorUsuario.getOrDefault(id, 0);
        if (!activo && clientes > 0 && !reasignarClientes) {
            throw new ApiException(409, u.nombre() + " tiene " + clientes + " cliente(s) asignado(s).",
                    Map.of(), Map.of("clientesAsignados", String.valueOf(clientes)));
        }
        if (!activo && reasignarClientes) {
            clientesPorUsuario.put(id, 0);
        }
        Usuario actualizado = new Usuario(id, u.nombre(), u.email(), u.rol(), u.gerenteId(), u.gerenteNombre(),
                activo, u.creadoEn());
        usuarios.put(id, actualizado);
        return actualizado;
    }

    @Override
    public synchronized void cambiarPassword(int id, String passwordActual, String passwordNueva) {
        obtener(id);
    }

    private Usuario obtener(int id) {
        Usuario usuario = usuarios.get(id);
        if (usuario == null) {
            throw new ApiException(404, "No existe el usuario con id " + id);
        }
        return usuario;
    }

    private String nombreDe(Integer id) {
        return id == null || !usuarios.containsKey(id) ? null : usuarios.get(id).nombre();
    }

    private void validarEmailUnico(String email, Integer idPropio) {
        boolean repetido = usuarios.values().stream()
                .anyMatch(u -> u.email().equalsIgnoreCase(email) && !u.id().equals(idPropio));
        if (repetido) {
            throw new ApiException(409, "Ya existe un usuario con el email " + email + ".");
        }
    }

    private void agregar(String nombre, String email, Rol rol, Integer gerenteId, boolean activo, int clientes) {
        int id = siguienteId++;
        usuarios.put(id, new Usuario(id, nombre, email, rol, gerenteId, nombreDe(gerenteId), activo,
                LocalDateTime.of(2025, 9, 1, 9, 0)));
        clientesPorUsuario.put(id, clientes);
    }
}
