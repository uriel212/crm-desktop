package com.devsuj.crmdesktop.client;

import com.devsuj.crmdesktop.model.CambioEtapaRequest;
import com.devsuj.crmdesktop.model.EtapaOportunidad;
import com.devsuj.crmdesktop.model.HistorialEtapa;
import com.devsuj.crmdesktop.model.Oportunidad;
import com.devsuj.crmdesktop.model.OportunidadActualizarRequest;
import com.devsuj.crmdesktop.model.OportunidadRequest;
import com.devsuj.crmdesktop.model.Pagina;
import tools.jackson.core.type.TypeReference;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** {@link OportunidadApi} que llama a la API REST. */
public class OportunidadApiHttp implements OportunidadApi {

    private static final String RUTA = "/oportunidades";
    private static final TypeReference<Pagina<Oportunidad>> PAGINA = new TypeReference<>() {};
    private static final TypeReference<Oportunidad> OPORTUNIDAD = new TypeReference<>() {};
    private static final TypeReference<List<HistorialEtapa>> HISTORIAL = new TypeReference<>() {};

    private final ApiHttp api;

    public OportunidadApiHttp(ApiHttp api) {
        this.api = api;
    }

    @Override
    public Pagina<Oportunidad> buscar(EtapaOportunidad etapa, Integer usuarioId, Integer clienteId, Boolean abiertas,
                                      int pagina, int tamano) {
        Map<String, Object> parametros = new LinkedHashMap<>();
        parametros.put("etapa", etapa);
        parametros.put("usuarioId", usuarioId);
        parametros.put("clienteId", clienteId);
        parametros.put("abiertas", abiertas);
        parametros.put("page", pagina);
        parametros.put("size", tamano);
        return api.get(RUTA, parametros, PAGINA);
    }

    @Override
    public List<HistorialEtapa> historial(int id) {
        return api.get(RUTA + "/" + id + "/historial", Map.of(), HISTORIAL);
    }

    @Override
    public Oportunidad crear(OportunidadRequest datos) {
        return api.post(RUTA, datos, OPORTUNIDAD);
    }

    @Override
    public Oportunidad actualizar(int id, OportunidadActualizarRequest datos) {
        return api.put(RUTA + "/" + id, datos, OPORTUNIDAD);
    }

    @Override
    public Oportunidad cambiarEtapa(int id, EtapaOportunidad etapa, String comentario) {
        return api.patch(RUTA + "/" + id + "/etapa", new CambioEtapaRequest(etapa, comentario), OPORTUNIDAD);
    }
}
