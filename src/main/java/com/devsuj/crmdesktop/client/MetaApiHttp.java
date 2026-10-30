package com.devsuj.crmdesktop.client;

import com.devsuj.crmdesktop.model.MetaVenta;
import com.devsuj.crmdesktop.model.MetaVentaRequest;
import com.devsuj.crmdesktop.model.Pagina;
import tools.jackson.core.type.TypeReference;

import java.util.LinkedHashMap;
import java.util.Map;

/** {@link MetaApi} que llama a la API REST. */
public class MetaApiHttp implements MetaApi {

    private static final String RUTA = "/metas";
    private static final TypeReference<Pagina<MetaVenta>> PAGINA = new TypeReference<>() {};
    private static final TypeReference<MetaVenta> META = new TypeReference<>() {};

    private final ApiHttp api;

    public MetaApiHttp(ApiHttp api) {
        this.api = api;
    }

    @Override
    public Pagina<MetaVenta> buscar(Integer usuarioId, Integer anio, Integer mes, int pagina, int tamano) {
        Map<String, Object> parametros = new LinkedHashMap<>();
        parametros.put("usuarioId", usuarioId);
        parametros.put("anio", anio);
        parametros.put("mes", mes);
        parametros.put("page", pagina);
        parametros.put("size", tamano);
        return api.get(RUTA, parametros, PAGINA);
    }

    @Override
    public MetaVenta crear(MetaVentaRequest datos) {
        return api.post(RUTA, datos, META);
    }

    @Override
    public MetaVenta actualizar(int id, MetaVentaRequest datos) {
        return api.put(RUTA + "/" + id, datos, META);
    }

    @Override
    public void eliminar(int id) {
        api.delete(RUTA + "/" + id);
    }
}
