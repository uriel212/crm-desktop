package com.devsuj.crmdesktop.client;

import com.devsuj.crmdesktop.model.CanjeRequest;
import com.devsuj.crmdesktop.model.CanjeResponse;
import com.devsuj.crmdesktop.model.MovimientoPuntos;
import com.devsuj.crmdesktop.model.Pagina;
import com.devsuj.crmdesktop.model.SaldoPuntos;
import tools.jackson.core.type.TypeReference;

import java.util.Map;

/** {@link FidelizacionApi} que llama a la API REST. */
public class FidelizacionApiHttp implements FidelizacionApi {

    private static final TypeReference<SaldoPuntos> SALDO = new TypeReference<>() {};
    private static final TypeReference<Pagina<MovimientoPuntos>> MOVIMIENTOS = new TypeReference<>() {};
    private static final TypeReference<CanjeResponse> CANJE = new TypeReference<>() {};

    private final ApiHttp api;

    public FidelizacionApiHttp(ApiHttp api) {
        this.api = api;
    }

    private static String ruta(int clienteId) {
        return "/clientes/" + clienteId + "/puntos";
    }

    @Override
    public SaldoPuntos saldo(int clienteId) {
        return api.get(ruta(clienteId), Map.of(), SALDO);
    }

    @Override
    public Pagina<MovimientoPuntos> movimientos(int clienteId, int pagina, int tamano) {
        return api.get(ruta(clienteId) + "/movimientos", Map.of("page", pagina, "size", tamano), MOVIMIENTOS);
    }

    @Override
    public CanjeResponse canjear(int clienteId, CanjeRequest datos) {
        return api.post(ruta(clienteId) + "/canjes", datos, CANJE);
    }
}
