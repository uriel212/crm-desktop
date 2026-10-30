package com.devsuj.crmdesktop.service;

import com.devsuj.crmdesktop.client.ClienteApi;
import com.devsuj.crmdesktop.model.Cliente;
import com.devsuj.crmdesktop.model.ClienteRequest;
import com.devsuj.crmdesktop.model.EstadoCliente;
import com.devsuj.crmdesktop.model.Pagina;
import com.devsuj.crmdesktop.model.ReasignacionResponse;
import com.devsuj.crmdesktop.model.ResumenCliente;
import com.devsuj.crmdesktop.model.Usuario;

import java.util.List;
import java.util.Map;

/**
 * Lógica de la pantalla de clientes: valida, limpia los datos y coordina las llamadas a la API.
 *
 * <p>No sabe nada de JavaFX (S de SOLID): el controlador se encarga de la vista y este service de
 * las reglas. Recibe un {@link ClienteApi} por constructor, así en las pruebas se le pasa una
 * versión en memoria.</p>
 */
public class ClienteService {

    public static final int TAMANO_PAGINA = 20;

    private final ClienteApi api;

    public ClienteService(ClienteApi api) {
        this.api = api;
    }

    /** @param pagina empieza en 0 */
    public Pagina<Cliente> buscar(String texto, EstadoCliente estado, int pagina) {
        return buscar(texto, estado, null, pagina);
    }

    /** @param vendedorId solo los clientes de ese responsable; {@code null} = todos */
    public Pagina<Cliente> buscar(String texto, EstadoCliente estado, Integer vendedorId, int pagina) {
        return api.buscar(limpiar(texto), estado, vendedorId, pagina, TAMANO_PAGINA);
    }

    /**
     * Clientes para elegir en un combo (por ejemplo, al registrar una oportunidad): los primeros 100
     * activos o prospectos que coinciden con el texto, ordenados por nombre.
     */
    public List<Cliente> paraSeleccion(String texto) {
        return api.buscar(limpiar(texto), null, null, 0, 100).contenido().stream()
                .filter(c -> c.estado() != EstadoCliente.INACTIVO)
                .toList();
    }

    /**
     * Crea un cliente nuevo ({@code original == null}) o actualiza uno existente.
     *
     * @throws ValidacionException si los datos no cumplen las reglas (no se llama a la API)
     */
    public Cliente guardar(Cliente original, ClienteFormulario datos) {
        Map<String, String> errores = ValidadorCliente.validar(datos);
        if (!errores.isEmpty()) {
            throw new ValidacionException(errores);
        }

        // vendedorId = null: al crear, la API pone como responsable al VENDEDOR que lo registra; al
        // editar, null significa "no cambia". El responsable se cambia con asignarVendedor().
        ClienteRequest request = new ClienteRequest(
                limpiar(datos.nombre()), limpiar(datos.empresa()), limpiar(datos.email()),
                limpiar(datos.telefono()), limpiar(datos.direccion()), null);

        Cliente guardado = original == null ? api.crear(request) : api.actualizar(original.id(), request);

        // El estado no viaja en ClienteRequest: la API lo cambia con su propio endpoint (PATCH).
        if (datos.estado() != null && datos.estado() != guardado.estado()) {
            guardado = api.cambiarEstado(guardado.id(), datos.estado());
        }
        return guardado;
    }

    /**
     * "Baja" lógica: la API no borra clientes (son parte del historial de ventas, contratos, etc.),
     * los pasa a estado INACTIVO.
     */
    public Cliente darDeBaja(Cliente cliente) {
        if (cliente.estado() == EstadoCliente.INACTIVO) {
            throw new IllegalStateException("El cliente " + cliente.nombre() + " ya está inactivo.");
        }
        return api.cambiarEstado(cliente.id(), EstadoCliente.INACTIVO);
    }

    /**
     * Cambia el responsable de uno o varios clientes. Con uno solo usa {@code PATCH /vendedor}
     * (permite dejarlo sin responsable); con varios, la reasignación en lote.
     *
     * @param nuevo responsable; {@code null} = sin responsable (solo para un cliente)
     * @return cuántos clientes cambiaron
     */
    public int asignarVendedor(List<Cliente> clientes, Usuario nuevo) {
        if (clientes.isEmpty()) {
            throw new IllegalStateException("Selecciona al menos un cliente.");
        }
        if (clientes.size() == 1) {
            api.asignarVendedor(clientes.getFirst().id(), nuevo == null ? null : nuevo.id());
            return 1;
        }
        if (nuevo == null) {
            throw new IllegalStateException("Para varios clientes a la vez elige un responsable.");
        }
        ReasignacionResponse respuesta = api.reasignar(clientes.stream().map(Cliente::id).toList(), nuevo.id());
        return respuesta.reasignados();
    }

    /** Vista 360: compras, pipeline, actividad, puntos, contratos, tickets y campañas del cliente. */
    public ResumenCliente resumen(Cliente cliente) {
        return api.resumen(cliente.id());
    }

    /** Quita espacios sobrantes y convierte los textos vacíos en null (campo opcional sin valor). */
    private static String limpiar(String texto) {
        return Reglas.limpiar(texto);
    }
}
