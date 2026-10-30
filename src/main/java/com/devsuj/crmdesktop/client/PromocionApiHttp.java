package com.devsuj.crmdesktop.client;

import com.devsuj.crmdesktop.model.AlcancePromocion;
import com.devsuj.crmdesktop.model.CambioEstadoPromocionRequest;
import com.devsuj.crmdesktop.model.Pagina;
import com.devsuj.crmdesktop.model.Promocion;
import com.devsuj.crmdesktop.model.PromocionRequest;
import tools.jackson.core.type.TypeReference;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** {@link PromocionApi} que llama a la API REST. */
public class PromocionApiHttp implements PromocionApi {

    private static final String RUTA = "/promociones";
    private static final TypeReference<Pagina<Promocion>> PAGINA = new TypeReference<>() {};
    private static final TypeReference<List<Promocion>> LISTA = new TypeReference<>() {};
    private static final TypeReference<Promocion> PROMOCION = new TypeReference<>() {};

    private final ApiHttp api;

    public PromocionApiHttp(ApiHttp api) {
        this.api = api;
    }

    @Override
    public Pagina<Promocion> buscar(Boolean activa, AlcancePromocion alcance, Integer campanaId, Integer productoId,
                                    int pagina, int tamano) {
        Map<String, Object> parametros = new LinkedHashMap<>();
        parametros.put("activa", activa);
        parametros.put("alcance", alcance);
        parametros.put("campanaId", campanaId);
        parametros.put("productoId", productoId);
        parametros.put("page", pagina);
        parametros.put("size", tamano);
        return api.get(RUTA, parametros, PAGINA);
    }

    @Override
    public List<Promocion> vigentes() {
        return api.get(RUTA + "/vigentes", Map.of(), LISTA);
    }

    @Override
    public Promocion crear(PromocionRequest datos) {
        return api.post(RUTA, datos, PROMOCION);
    }

    @Override
    public Promocion actualizar(int id, PromocionRequest datos) {
        return api.put(RUTA + "/" + id, datos, PROMOCION);
    }

    @Override
    public Promocion cambiarEstado(int id, boolean activa) {
        return api.patch(RUTA + "/" + id + "/estado", new CambioEstadoPromocionRequest(activa), PROMOCION);
    }

    @Override
    public void eliminar(int id) {
        api.delete(RUTA + "/" + id);
    }
}
