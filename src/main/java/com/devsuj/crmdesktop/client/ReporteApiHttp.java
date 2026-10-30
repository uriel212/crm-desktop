package com.devsuj.crmdesktop.client;

import com.devsuj.crmdesktop.model.Dashboard;
import com.devsuj.crmdesktop.model.MetricasCampana;
import com.devsuj.crmdesktop.model.PipelineEtapa;
import com.devsuj.crmdesktop.model.ProductoVendido;
import com.devsuj.crmdesktop.model.RendimientoVendedor;
import com.devsuj.crmdesktop.model.ReporteFidelizacion;
import com.devsuj.crmdesktop.model.ReporteSoporte;
import com.devsuj.crmdesktop.model.VentasPorCategoria;
import com.devsuj.crmdesktop.model.VentasPorMes;
import com.devsuj.crmdesktop.model.VentasPorVendedor;
import tools.jackson.core.type.TypeReference;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** {@link ReporteApi} que llama a la API REST. */
public class ReporteApiHttp implements ReporteApi {

    private static final String RUTA = "/reportes";

    private final ApiHttp api;

    public ReporteApiHttp(ApiHttp api) {
        this.api = api;
    }

    @Override
    public Dashboard dashboard(LocalDate desde, LocalDate hasta) {
        return api.get(RUTA + "/dashboard", periodo(desde, hasta), new TypeReference<>() {});
    }

    @Override
    public List<VentasPorMes> ventasPorMes(int anio) {
        return api.get(RUTA + "/ventas/por-mes", Map.of("anio", anio), new TypeReference<>() {});
    }

    @Override
    public List<VentasPorVendedor> ventasPorVendedor(LocalDate desde, LocalDate hasta) {
        return api.get(RUTA + "/ventas/por-vendedor", periodo(desde, hasta), new TypeReference<>() {});
    }

    @Override
    public List<VentasPorCategoria> ventasPorCategoria(LocalDate desde, LocalDate hasta) {
        return api.get(RUTA + "/ventas/por-categoria", periodo(desde, hasta), new TypeReference<>() {});
    }

    @Override
    public List<ProductoVendido> masVendidos(LocalDate desde, LocalDate hasta, int limite) {
        Map<String, Object> parametros = periodo(desde, hasta);
        parametros.put("limite", limite);
        return api.get(RUTA + "/productos/mas-vendidos", parametros, new TypeReference<>() {});
    }

    @Override
    public List<PipelineEtapa> pipeline() {
        return api.get(RUTA + "/oportunidades/pipeline", Map.of(), new TypeReference<>() {});
    }

    @Override
    public List<RendimientoVendedor> rendimientoVendedores(int anio, int mes) {
        return api.get(RUTA + "/rendimiento-vendedores", Map.of("anio", anio, "mes", mes), new TypeReference<>() {});
    }

    @Override
    public List<MetricasCampana> efectividadCampanas() {
        return api.get(RUTA + "/campanas/efectividad", Map.of(), new TypeReference<>() {});
    }

    @Override
    public ReporteSoporte soporte() {
        return api.get(RUTA + "/soporte", Map.of(), new TypeReference<>() {});
    }

    @Override
    public ReporteFidelizacion fidelizacion() {
        return api.get(RUTA + "/fidelizacion", Map.of(), new TypeReference<>() {});
    }

    private static Map<String, Object> periodo(LocalDate desde, LocalDate hasta) {
        Map<String, Object> parametros = new LinkedHashMap<>();
        parametros.put("desde", desde);
        parametros.put("hasta", hasta);
        return parametros;
    }
}
