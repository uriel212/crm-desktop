package com.devsuj.crmdesktop.client;

import com.devsuj.crmdesktop.model.AsignarVendedorRequest;
import com.devsuj.crmdesktop.model.CambioEstadoClienteRequest;
import com.devsuj.crmdesktop.model.Cliente;
import com.devsuj.crmdesktop.model.ClienteRequest;
import com.devsuj.crmdesktop.model.EstadoCliente;
import com.devsuj.crmdesktop.model.Pagina;
import com.devsuj.crmdesktop.model.ReasignacionLoteRequest;
import com.devsuj.crmdesktop.model.ReasignacionResponse;
import com.devsuj.crmdesktop.model.ResumenCliente;
import tools.jackson.core.type.TypeReference;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link ClienteApi} que llama a la API REST. Solo arma rutas y parámetros: lo demás
 * (JSON, headers, errores) lo resuelve {@link ApiHttp}.
 */
public class ClienteApiHttp implements ClienteApi {

    private static final String RUTA = "/clientes";

    // TypeReference le dice a Jackson el tipo genérico exacto (Pagina<Cliente>), que Java "borra" al compilar.
    private static final TypeReference<Pagina<Cliente>> PAGINA_DE_CLIENTES = new TypeReference<>() {};
    private static final TypeReference<Cliente> CLIENTE = new TypeReference<>() {};
    private static final TypeReference<ReasignacionResponse> REASIGNACION = new TypeReference<>() {};
    private static final TypeReference<ResumenCliente> RESUMEN = new TypeReference<>() {};

    private final ApiHttp api;

    public ClienteApiHttp(ApiHttp api) {
        this.api = api;
    }

    @Override
    public Pagina<Cliente> buscar(String texto, EstadoCliente estado, Integer vendedorId, int pagina, int tamano) {
        Map<String, Object> parametros = new LinkedHashMap<>();
        parametros.put("q", texto);
        parametros.put("estado", estado);
        parametros.put("vendedorId", vendedorId);
        parametros.put("page", pagina);
        parametros.put("size", tamano);
        parametros.put("sort", "nombre");
        return api.get(RUTA, parametros, PAGINA_DE_CLIENTES);
    }

    @Override
    public Cliente obtener(int id) {
        return api.get(RUTA + "/" + id, Map.of(), CLIENTE);
    }

    @Override
    public Cliente crear(ClienteRequest datos) {
        return api.post(RUTA, datos, CLIENTE);
    }

    @Override
    public Cliente actualizar(int id, ClienteRequest datos) {
        return api.put(RUTA + "/" + id, datos, CLIENTE);
    }

    @Override
    public Cliente cambiarEstado(int id, EstadoCliente estado) {
        return api.patch(RUTA + "/" + id + "/estado", new CambioEstadoClienteRequest(estado), CLIENTE);
    }

    @Override
    public Cliente asignarVendedor(int id, Integer vendedorId) {
        return api.patch(RUTA + "/" + id + "/vendedor", new AsignarVendedorRequest(vendedorId), CLIENTE);
    }

    @Override
    public ResumenCliente resumen(int id) {
        return api.get(RUTA + "/" + id + "/resumen", Map.of(), RESUMEN);
    }

    @Override
    public ReasignacionResponse reasignar(List<Integer> clienteIds, int vendedorId) {
        return api.post(RUTA + "/reasignaciones", new ReasignacionLoteRequest(clienteIds, vendedorId), REASIGNACION);
    }
}
