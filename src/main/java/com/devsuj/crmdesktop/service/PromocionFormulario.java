package com.devsuj.crmdesktop.service;

import com.devsuj.crmdesktop.model.AlcancePromocion;
import com.devsuj.crmdesktop.model.Campana;
import com.devsuj.crmdesktop.model.Producto;
import com.devsuj.crmdesktop.model.TipoPromocion;

import java.time.LocalDate;
import java.util.List;

/**
 * Lo que el usuario escribió en el formulario de promoción (sin validar).
 *
 * @param campana     opcional: la promoción solo aplica mientras esa campaña está ACTIVA
 * @param productos   solo para alcance PRODUCTO
 * @param montoMinimo solo para alcance VENTA (texto; vacío = sin mínimo)
 */
public record PromocionFormulario(Campana campana, String nombre, TipoPromocion tipo, AlcancePromocion alcance,
                                  String valor, String montoMinimo, LocalDate fechaInicio, LocalDate fechaFin,
                                  List<Producto> productos) {
}
