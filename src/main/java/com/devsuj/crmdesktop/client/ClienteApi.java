package com.devsuj.crmdesktop.client;

import com.devsuj.crmdesktop.model.Cliente;
import com.devsuj.crmdesktop.model.ClienteRequest;
import com.devsuj.crmdesktop.model.EstadoCliente;
import com.devsuj.crmdesktop.model.Pagina;
import com.devsuj.crmdesktop.model.ReasignacionResponse;
import com.devsuj.crmdesktop.model.ResumenCliente;

import java.util.List;

/**
 * Operaciones del recurso {@code /api/clientes}.
 *
 * <p>Es una interfaz para poder tener varias implementaciones intercambiables (L e I de SOLID):
 * {@link ClienteApiHttp} habla con la API real y {@link ClienteApiEnMemoria} sirve para pruebas
 * y para el modo demostración. El service no sabe cuál está usando.</p>
 *
 * <p>Todas las operaciones lanzan {@link ApiException} si algo sale mal.</p>
 */
public interface ClienteApi {

    /**
     * {@code GET /api/clientes?q=&estado=&vendedorId=&page=&size=}
     *
     * @param vendedorId solo los clientes de ese responsable; {@code null} = todos
     */
    Pagina<Cliente> buscar(String texto, EstadoCliente estado, Integer vendedorId, int pagina, int tamano);

    /** {@code GET /api/clientes/{id}} */
    Cliente obtener(int id);

    /** {@code POST /api/clientes} (la API responde 201 con el cliente creado) */
    Cliente crear(ClienteRequest datos);

    /** {@code PUT /api/clientes/{id}} */
    Cliente actualizar(int id, ClienteRequest datos);

    /** {@code PATCH /api/clientes/{id}/estado} */
    Cliente cambiarEstado(int id, EstadoCliente estado);

    /** {@code PATCH /api/clientes/{id}/vendedor} ({@code null} = sin responsable). Solo GERENTE y ADMIN. */
    Cliente asignarVendedor(int id, Integer vendedorId);

    /** {@code POST /api/clientes/reasignaciones}. Solo GERENTE y ADMIN. */
    ReasignacionResponse reasignar(List<Integer> clienteIds, int vendedorId);

    /** {@code GET /api/clientes/{id}/resumen}: vista 360 (vendedor del cliente, su gerente o ADMIN). */
    ResumenCliente resumen(int id);
}
