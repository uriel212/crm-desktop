package com.devsuj.crmdesktop.client;

import com.devsuj.crmdesktop.model.CambioEstadoContratoRequest;
import com.devsuj.crmdesktop.model.Contrato;
import com.devsuj.crmdesktop.model.ContratoRequest;
import com.devsuj.crmdesktop.model.EstadoContrato;
import com.devsuj.crmdesktop.model.ExtensionContratoRequest;
import com.devsuj.crmdesktop.model.Pagina;
import com.devsuj.crmdesktop.model.RenovacionContratoRequest;
import com.devsuj.crmdesktop.model.VencimientoLote;
import tools.jackson.core.type.TypeReference;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/** {@link ContratoApi} que llama a la API REST. */
public class ContratoApiHttp implements ContratoApi {

    private static final String RUTA = "/contratos";
    private static final TypeReference<Pagina<Contrato>> PAGINA = new TypeReference<>() {};
    private static final TypeReference<Contrato> CONTRATO = new TypeReference<>() {};
    private static final TypeReference<VencimientoLote> VENCIMIENTOS = new TypeReference<>() {};

    private final ApiHttp api;

    public ContratoApiHttp(ApiHttp api) {
        this.api = api;
    }

    @Override
    public Pagina<Contrato> buscar(Integer clienteId, EstadoContrato estado, Integer porVencerDias, int pagina, int tamano) {
        Map<String, Object> parametros = new LinkedHashMap<>();
        parametros.put("clienteId", clienteId);
        parametros.put("estado", estado);
        parametros.put("porVencerDias", porVencerDias);
        parametros.put("page", pagina);
        parametros.put("size", tamano);
        return api.get(RUTA, parametros, PAGINA);
    }

    @Override
    public Contrato crear(ContratoRequest datos) {
        return api.post(RUTA, datos, CONTRATO);
    }

    @Override
    public Contrato actualizar(int id, ContratoRequest datos) {
        return api.put(RUTA + "/" + id, datos, CONTRATO);
    }

    @Override
    public Contrato cambiarEstado(int id, EstadoContrato estado) {
        return api.patch(RUTA + "/" + id + "/estado", new CambioEstadoContratoRequest(estado), CONTRATO);
    }

    @Override
    public Contrato extender(int id, LocalDate nuevaFechaFin) {
        return api.patch(RUTA + "/" + id + "/extension", new ExtensionContratoRequest(nuevaFechaFin), CONTRATO);
    }

    @Override
    public Contrato renovar(int id, RenovacionContratoRequest datos) {
        return api.post(RUTA + "/" + id + "/renovacion", datos, CONTRATO);
    }

    @Override
    public VencimientoLote marcarVencidos() {
        return api.post(RUTA + "/vencimientos", Map.of(), VENCIMIENTOS);
    }
}
