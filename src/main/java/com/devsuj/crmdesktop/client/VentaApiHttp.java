package com.devsuj.crmdesktop.client;

import com.devsuj.crmdesktop.model.CalculoVenta;
import com.devsuj.crmdesktop.model.Pagina;
import com.devsuj.crmdesktop.model.Venta;
import com.devsuj.crmdesktop.model.VentaRequest;
import com.devsuj.crmdesktop.model.VentaResumen;
import tools.jackson.core.type.TypeReference;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/** {@link VentaApi} que llama a la API REST. */
public class VentaApiHttp implements VentaApi {

    private static final String RUTA = "/ventas";
    private static final TypeReference<Pagina<VentaResumen>> PAGINA = new TypeReference<>() {};
    private static final TypeReference<Venta> VENTA = new TypeReference<>() {};
    private static final TypeReference<CalculoVenta> CALCULO = new TypeReference<>() {};

    private final ApiHttp api;

    public VentaApiHttp(ApiHttp api) {
        this.api = api;
    }

    @Override
    public Pagina<VentaResumen> buscar(Integer clienteId, Integer usuarioId, LocalDate desde, LocalDate hasta,
                                       int pagina, int tamano) {
        Map<String, Object> parametros = new LinkedHashMap<>();
        parametros.put("clienteId", clienteId);
        parametros.put("usuarioId", usuarioId);
        parametros.put("desde", desde);
        parametros.put("hasta", hasta);
        parametros.put("page", pagina);
        parametros.put("size", tamano);
        return api.get(RUTA, parametros, PAGINA);
    }

    @Override
    public Venta obtener(int id) {
        return api.get(RUTA + "/" + id, Map.of(), VENTA);
    }

    @Override
    public CalculoVenta calcular(VentaRequest datos) {
        return api.post(RUTA + "/calculo", datos, CALCULO);
    }

    @Override
    public Venta registrar(VentaRequest datos) {
        return api.post(RUTA, datos, VENTA);
    }
}
