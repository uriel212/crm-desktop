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

import java.time.LocalDate;
import java.util.List;

/** Reportes de solo lectura para el tablero ({@code /api/reportes}), filtrados por el alcance del usuario. */
public interface ReporteApi {

    Dashboard dashboard(LocalDate desde, LocalDate hasta);

    List<VentasPorMes> ventasPorMes(int anio);

    List<VentasPorVendedor> ventasPorVendedor(LocalDate desde, LocalDate hasta);

    List<VentasPorCategoria> ventasPorCategoria(LocalDate desde, LocalDate hasta);

    List<ProductoVendido> masVendidos(LocalDate desde, LocalDate hasta, int limite);

    List<PipelineEtapa> pipeline();

    List<RendimientoVendedor> rendimientoVendedores(int anio, int mes);

    /** {@code GET /api/reportes/campanas/efectividad}: métricas de todas las campañas. Solo GERENTE y ADMIN. */
    List<MetricasCampana> efectividadCampanas();

    /** {@code GET /api/reportes/soporte} */
    ReporteSoporte soporte();

    /** {@code GET /api/reportes/fidelizacion} */
    ReporteFidelizacion fidelizacion();
}
