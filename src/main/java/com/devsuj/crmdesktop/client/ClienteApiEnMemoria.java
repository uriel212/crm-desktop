package com.devsuj.crmdesktop.client;

import com.devsuj.crmdesktop.model.Cliente;
import com.devsuj.crmdesktop.model.ClienteRequest;
import com.devsuj.crmdesktop.model.EstadoCliente;
import com.devsuj.crmdesktop.model.Pagina;
import com.devsuj.crmdesktop.model.ReasignacionResponse;
import com.devsuj.crmdesktop.model.ResumenCliente;

import java.math.BigDecimal;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * {@link ClienteApi} "falsa" que guarda los clientes en memoria (se pierden al cerrar la app).
 *
 * <p>Se usa en las pruebas unitarias y en el modo demostración ({@code api.datos-de-prueba=true}),
 * para poder usar la interfaz aunque la API no tenga todavía los endpoints de clientes.
 * Imita las reglas de la API: email único (409), cliente inexistente (404), estado inicial PROSPECTO.</p>
 */
public class ClienteApiEnMemoria implements ClienteApi {

    private final Map<Integer, Cliente> clientes = new LinkedHashMap<>();
    private int siguienteId = 1;

    /** Los mismos clientes y vendedores del seed de la base de datos ({@code V2__seed.sql}). */
    public static ClienteApiEnMemoria conDatosDeEjemplo() {
        String carla = "Carla Mendoza", jorge = "Jorge Pérez", sofia = "Sofía Herrera", mario = "Mario Ruiz";
        ClienteApiEnMemoria api = new ClienteApiEnMemoria();
        api.agregar("Roberto Garza", "Grupo Industrial del Norte", "rgarza@gin.test", "8112345678", "Av. Constitución 1500, Monterrey, NL", EstadoCliente.ACTIVO, 3, carla);
        api.agregar("Mariana López", "Despacho López y Asociados", "mlopez@lopezasoc.test", "5512345601", "Insurgentes Sur 1200, CDMX", EstadoCliente.ACTIVO, 3, carla);
        api.agregar("Fernando Castillo", "Clínica Santa Fe", "fcastillo@clinicasantafe.test", "3312345602", "Av. Vallarta 3200, Guadalajara, Jal.", EstadoCliente.ACTIVO, 4, jorge);
        api.agregar("Patricia Núñez", "Colegio Montessori Valle", "pnunez@montessorivalle.test", "4421234503", "Blvd. Bernardo Quintana 400, Querétaro, Qro.", EstadoCliente.ACTIVO, 4, jorge);
        api.agregar("Alejandro Ríos", "Transportes Ríos", "arios@transrios.test", "8112345604", "Carretera a Laredo km 12, Escobedo, NL", EstadoCliente.ACTIVO, 5, sofia);
        api.agregar("Daniela Ortega", null, "dortega@correo.test", "5512345605", "Calle Durango 45, Roma Norte, CDMX", EstadoCliente.ACTIVO, 5, sofia);
        api.agregar("Héctor Salinas", "Constructora Salinas", "hsalinas@csalinas.test", "2221234506", "Av. Juárez 2100, Puebla, Pue.", EstadoCliente.ACTIVO, 3, carla);
        api.agregar("Lucía Fernández", "Agencia Creativa Pixel", "lfernandez@pixel.test", "5512345607", "Álvaro Obregón 120, Roma Norte, CDMX", EstadoCliente.ACTIVO, 4, jorge);
        api.agregar("Ricardo Méndez", "Farmacias Méndez", "rmendez@farmendez.test", "9991234508", "Calle 60 450, Centro, Mérida, Yuc.", EstadoCliente.INACTIVO, 6, mario);
        api.agregar("Gabriela Soto", "Hotel Costa Azul", "gsoto@costaazul.test", "9981234509", "Blvd. Kukulcán km 9, Cancún, Q. Roo", EstadoCliente.ACTIVO, 5, sofia);
        api.agregar("Miguel Ángel Vega", "Restaurante El Fogón", "mvega@elfogon.test", "3312345610", "Av. Chapultepec 210, Guadalajara, Jal.", EstadoCliente.PROSPECTO, 3, carla);
        api.agregar("Valeria Cruz", "Nube Digital", "vcruz@nubedigital.test", "5512345611", "Paseo de la Reforma 300, CDMX", EstadoCliente.PROSPECTO, 4, jorge);
        api.agregar("Andrés Morales", "Universidad Tecnológica del Bajío", "amorales@utb.test", "4771234512", "Blvd. Torres Landa 800, León, Gto.", EstadoCliente.PROSPECTO, 5, sofia);
        api.agregar("Elena Rangel", "Notaría Pública 15", "erangel@notaria15.test", "6141234513", "Av. Independencia 900, Chihuahua, Chih.", EstadoCliente.PROSPECTO, null, null);
        api.agregar("Tomás Aguilar", "Taller Mecánico Aguilar", "taguilar@talleraguilar.test", "8112345614", "Calle Zaragoza 77, Centro, Monterrey, NL", EstadoCliente.INACTIVO, 3, carla);
        return api;
    }

    @Override
    public synchronized Pagina<Cliente> buscar(String texto, EstadoCliente estado, Integer vendedorId,
                                               int pagina, int tamano) {
        String filtro = texto == null ? "" : texto.strip().toLowerCase(Locale.ROOT);
        List<Cliente> encontrados = clientes.values().stream()
                .filter(c -> estado == null || c.estado() == estado)
                .filter(c -> vendedorId == null || vendedorId.equals(c.vendedorId()))
                .filter(c -> filtro.isEmpty() || contiene(c.nombre(), filtro)
                        || contiene(c.empresa(), filtro) || contiene(c.email(), filtro))
                .sorted(Comparator.comparing(Cliente::nombre))
                .toList();

        int desde = Math.min(pagina * tamano, encontrados.size());
        int hasta = Math.min(desde + tamano, encontrados.size());
        int totalPaginas = (int) Math.ceil(encontrados.size() / (double) tamano);
        return new Pagina<>(encontrados.subList(desde, hasta), pagina, tamano, encontrados.size(), totalPaginas);
    }

    @Override
    public synchronized Cliente obtener(int id) {
        Cliente cliente = clientes.get(id);
        if (cliente == null) {
            throw new ApiException(404, "No existe el cliente con id " + id);
        }
        return cliente;
    }

    @Override
    public synchronized Cliente crear(ClienteRequest datos) {
        validarEmailUnico(datos.email(), null);
        Cliente nuevo = new Cliente(siguienteId++, datos.nombre(), datos.empresa(), datos.email(),
                datos.telefono(), datos.direccion(), EstadoCliente.PROSPECTO, datos.vendedorId(), null,
                LocalDateTime.now(), LocalDateTime.now());
        clientes.put(nuevo.id(), nuevo);
        return nuevo;
    }

    @Override
    public synchronized Cliente actualizar(int id, ClienteRequest datos) {
        Cliente actual = obtener(id);
        validarEmailUnico(datos.email(), id);
        // Igual que la API: vendedorId null = el responsable no cambia.
        Cliente actualizado = new Cliente(id, datos.nombre(), datos.empresa(), datos.email(), datos.telefono(),
                datos.direccion(), actual.estado(), actual.vendedorId(), actual.vendedorNombre(),
                actual.creadoEn(), LocalDateTime.now());
        clientes.put(id, actualizado);
        return actualizado;
    }

    @Override
    public synchronized Cliente cambiarEstado(int id, EstadoCliente estado) {
        Cliente c = obtener(id);
        Cliente actualizado = new Cliente(id, c.nombre(), c.empresa(), c.email(), c.telefono(), c.direccion(),
                estado, c.vendedorId(), c.vendedorNombre(), c.creadoEn(), LocalDateTime.now());
        clientes.put(id, actualizado);
        return actualizado;
    }

    @Override
    public synchronized Cliente asignarVendedor(int id, Integer vendedorId) {
        Cliente c = obtener(id);
        Cliente actualizado = new Cliente(id, c.nombre(), c.empresa(), c.email(), c.telefono(), c.direccion(),
                c.estado(), vendedorId, vendedorId == null ? null : "Vendedor " + vendedorId,
                c.creadoEn(), LocalDateTime.now());
        clientes.put(id, actualizado);
        return actualizado;
    }

    @Override
    public synchronized ReasignacionResponse reasignar(List<Integer> clienteIds, int vendedorId) {
        clienteIds.forEach(id -> asignarVendedor(id, vendedorId));
        return new ReasignacionResponse(clienteIds.size(), vendedorId, "Vendedor " + vendedorId);
    }

    /** En memoria no hay ventas ni actividad: el resumen solo trae el cliente y ceros. */
    @Override
    public synchronized ResumenCliente resumen(int id) {
        return new ResumenCliente(obtener(id), 0, BigDecimal.ZERO, BigDecimal.ZERO, null, List.of(), 0,
                BigDecimal.ZERO, null, 0, 0, 0, 0, 0);
    }

    private void agregar(String nombre, String empresa, String email, String telefono, String direccion,
                         EstadoCliente estado, Integer vendedorId, String vendedorNombre) {
        int id = siguienteId++;
        LocalDateTime ahora = LocalDateTime.now();
        clientes.put(id, new Cliente(id, nombre, empresa, email, telefono, direccion, estado,
                vendedorId, vendedorNombre, ahora, ahora));
    }

    private void validarEmailUnico(String email, Integer idPropio) {
        boolean repetido = clientes.values().stream()
                .anyMatch(c -> c.email().equalsIgnoreCase(email) && !c.id().equals(idPropio));
        if (repetido) {
            throw new ApiException(409, "Ya existe un cliente con el email " + email,
                    Map.of("email", "Ya está registrado en otro cliente"));
        }
    }

    private static boolean contiene(String valor, String filtro) {
        return valor != null && valor.toLowerCase(Locale.ROOT).contains(filtro);
    }

    /** Copia de todos los clientes (útil en pruebas). */
    public synchronized List<Cliente> todos() {
        return new ArrayList<>(clientes.values());
    }
}
