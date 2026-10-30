package com.devsuj.crmdesktop.client;

import com.devsuj.crmdesktop.model.CambioEstadoInteraccionRequest;
import com.devsuj.crmdesktop.model.EstadoInteraccion;
import com.devsuj.crmdesktop.model.Interaccion;
import com.devsuj.crmdesktop.model.InteraccionActualizarRequest;
import com.devsuj.crmdesktop.model.InteraccionRequest;
import com.devsuj.crmdesktop.model.Pagina;
import com.devsuj.crmdesktop.model.TipoInteraccion;
import tools.jackson.core.type.TypeReference;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** {@link InteraccionApi} que llama a la API REST. */
public class InteraccionApiHttp implements InteraccionApi {

    private static final String RUTA = "/interacciones";
    private static final TypeReference<Pagina<Interaccion>> PAGINA = new TypeReference<>() {};
    private static final TypeReference<List<Interaccion>> LISTA = new TypeReference<>() {};
    private static final TypeReference<Interaccion> INTERACCION = new TypeReference<>() {};

    private final ApiHttp api;

    public InteraccionApiHttp(ApiHttp api) {
        this.api = api;
    }

    @Override
    public Pagina<Interaccion> buscar(Integer usuarioId, Integer clienteId, TipoInteraccion tipo,
                                      EstadoInteraccion estado, LocalDate desde, LocalDate hasta,
                                      int pagina, int tamano) {
        Map<String, Object> parametros = new LinkedHashMap<>();
        parametros.put("usuarioId", usuarioId);
        parametros.put("clienteId", clienteId);
        parametros.put("tipo", tipo);
        parametros.put("estado", estado);
        parametros.put("desde", desde); // LocalDate.toString() ya es ISO: 2026-10-21
        parametros.put("hasta", hasta);
        parametros.put("page", pagina);
        parametros.put("size", tamano);
        return api.get(RUTA, parametros, PAGINA);
    }

    @Override
    public List<Interaccion> proximas(Integer usuarioId, int dias) {
        Map<String, Object> parametros = new LinkedHashMap<>();
        parametros.put("usuarioId", usuarioId);
        parametros.put("dias", dias);
        return api.get(RUTA + "/proximas", parametros, LISTA);
    }

    @Override
    public Interaccion crear(int clienteId, InteraccionRequest datos) {
        return api.post("/clientes/" + clienteId + RUTA, datos, INTERACCION);
    }

    @Override
    public Interaccion actualizar(int id, InteraccionActualizarRequest datos) {
        return api.put(RUTA + "/" + id, datos, INTERACCION);
    }

    @Override
    public Interaccion cambiarEstado(int id, EstadoInteraccion estado, String resultado) {
        return api.patch(RUTA + "/" + id + "/estado", new CambioEstadoInteraccionRequest(estado, resultado),
                INTERACCION);
    }
}
