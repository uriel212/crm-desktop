package com.devsuj.crmdesktop.service;

import com.devsuj.crmdesktop.client.ReporteApi;
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
import com.devsuj.crmdesktop.util.Formatos;

import java.time.LocalDate;
import java.util.List;

/** Datos del tablero de KPIs y del rendimiento de vendedores. Sin JavaFX. */
public class ReporteService {

    private final ReporteApi api;

    public ReporteService(ReporteApi api) {
        this.api = api;
    }

    /** Todo lo que muestra el tablero, cargado de una vez (en segundo plano). */
    public record Tablero(Dashboard kpis, List<VentasPorMes> porMes, List<PipelineEtapa> pipeline,
                          List<VentasPorVendedor> porVendedor, List<VentasPorCategoria> porCategoria,
                          List<ProductoVendido> masVendidos, ReporteSoporte soporte, ReporteFidelizacion fidelizacion,
                          List<MetricasCampana> campanas) {
    }

    /**
     * @param conCampanas incluir la efectividad de campañas (solo GERENTE y ADMIN pueden consultarla;
     *                    para un VENDEDOR la lista queda vacía y no se llama a la API)
     */
    public Tablero tablero(LocalDate desde, LocalDate hasta, boolean conCampanas) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new IllegalStateException("La fecha \"desde\" no puede ser posterior a \"hasta\".");
        }
        int anio = hasta != null ? hasta.getYear() : Formatos.hoy().getYear();
        return new Tablero(api.dashboard(desde, hasta), api.ventasPorMes(anio), api.pipeline(),
                api.ventasPorVendedor(desde, hasta), api.ventasPorCategoria(desde, hasta),
                api.masVendidos(desde, hasta, 5), api.soporte(), api.fidelizacion(),
                conCampanas ? api.efectividadCampanas() : List.of());
    }

    public List<RendimientoVendedor> rendimiento(int anio, int mes) {
        return api.rendimientoVendedores(anio, mes);
    }
}
