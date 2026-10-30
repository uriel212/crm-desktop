package com.devsuj.crmdesktop.client;

import com.devsuj.crmdesktop.model.CalculoVenta;
import com.devsuj.crmdesktop.model.Pagina;
import com.devsuj.crmdesktop.model.Venta;
import com.devsuj.crmdesktop.model.VentaRequest;
import com.devsuj.crmdesktop.model.VentaResumen;

import java.time.LocalDate;

/** Operaciones de {@code /api/ventas}. Una venta no se edita ni se cancela. */
public interface VentaApi {

    /** {@code GET /api/ventas?clienteId=&usuarioId=&desde=&hasta=&page=&size=} */
    Pagina<VentaResumen> buscar(Integer clienteId, Integer usuarioId, LocalDate desde, LocalDate hasta,
                                int pagina, int tamano);

    /** {@code GET /api/ventas/{id}}: con renglones, promociones y puntos ganados. */
    Venta obtener(int id);

    /** {@code POST /api/ventas/calculo}: cotiza sin guardar (los precios los pone la API). */
    CalculoVenta calcular(VentaRequest datos);

    /** {@code POST /api/ventas} (201). */
    Venta registrar(VentaRequest datos);
}
