package com.devsuj.crmdesktop.client;

import com.devsuj.crmdesktop.model.CambioEstadoCampanaRequest;
import com.devsuj.crmdesktop.model.CambioResultadoRequest;
import com.devsuj.crmdesktop.model.CanalCampana;
import com.devsuj.crmdesktop.model.Campana;
import com.devsuj.crmdesktop.model.CampanaRequest;
import com.devsuj.crmdesktop.model.Destinatario;
import com.devsuj.crmdesktop.model.DestinatarioRequest;
import com.devsuj.crmdesktop.model.EstadoCampana;
import com.devsuj.crmdesktop.model.GeneracionDestinatarios;
import com.devsuj.crmdesktop.model.MetricasCampana;
import com.devsuj.crmdesktop.model.Pagina;
import com.devsuj.crmdesktop.model.ResultadoCampana;
import tools.jackson.core.type.TypeReference;

import java.util.LinkedHashMap;
import java.util.Map;

/** {@link CampanaApi} que llama a la API REST. */
public class CampanaApiHttp implements CampanaApi {

    private static final String RUTA = "/campanas";
    private static final TypeReference<Pagina<Campana>> PAGINA = new TypeReference<>() {};
    private static final TypeReference<Campana> CAMPANA = new TypeReference<>() {};
    private static final TypeReference<Pagina<Destinatario>> DESTINATARIOS = new TypeReference<>() {};
    private static final TypeReference<Destinatario> DESTINATARIO = new TypeReference<>() {};
    private static final TypeReference<GeneracionDestinatarios> GENERACION = new TypeReference<>() {};
    private static final TypeReference<MetricasCampana> METRICAS = new TypeReference<>() {};

    private final ApiHttp api;

    public CampanaApiHttp(ApiHttp api) {
        this.api = api;
    }

    @Override
    public Pagina<Campana> buscar(EstadoCampana estado, CanalCampana canal, String texto, int pagina, int tamano) {
        Map<String, Object> parametros = new LinkedHashMap<>();
        parametros.put("estado", estado);
        parametros.put("canal", canal);
        parametros.put("q", texto);
        parametros.put("page", pagina);
        parametros.put("size", tamano);
        return api.get(RUTA, parametros, PAGINA);
    }

    @Override
    public Campana crear(CampanaRequest datos) {
        return api.post(RUTA, datos, CAMPANA);
    }

    @Override
    public Campana actualizar(int id, CampanaRequest datos) {
        return api.put(RUTA + "/" + id, datos, CAMPANA);
    }

    @Override
    public Campana cambiarEstado(int id, EstadoCampana estado) {
        return api.patch(RUTA + "/" + id + "/estado", new CambioEstadoCampanaRequest(estado), CAMPANA);
    }

    @Override
    public Pagina<Destinatario> destinatarios(int campanaId, ResultadoCampana resultado, int pagina, int tamano) {
        Map<String, Object> parametros = new LinkedHashMap<>();
        parametros.put("resultado", resultado);
        parametros.put("page", pagina);
        parametros.put("size", tamano);
        return api.get(RUTA + "/" + campanaId + "/destinatarios", parametros, DESTINATARIOS);
    }

    @Override
    public GeneracionDestinatarios generarDestinatarios(int campanaId) {
        return api.post(RUTA + "/" + campanaId + "/destinatarios/generar", Map.of(), GENERACION);
    }

    @Override
    public Destinatario agregarDestinatario(int campanaId, int clienteId) {
        return api.post(RUTA + "/" + campanaId + "/destinatarios", new DestinatarioRequest(clienteId), DESTINATARIO);
    }

    @Override
    public void quitarDestinatario(int campanaId, int clienteId) {
        api.delete(RUTA + "/" + campanaId + "/destinatarios/" + clienteId);
    }

    @Override
    public Destinatario cambiarResultado(int campanaId, int clienteId, ResultadoCampana resultado) {
        return api.patch(RUTA + "/" + campanaId + "/destinatarios/" + clienteId + "/resultado",
                new CambioResultadoRequest(resultado), DESTINATARIO);
    }

    @Override
    public MetricasCampana metricas(int campanaId) {
        return api.get(RUTA + "/" + campanaId + "/metricas", Map.of(), METRICAS);
    }
}
