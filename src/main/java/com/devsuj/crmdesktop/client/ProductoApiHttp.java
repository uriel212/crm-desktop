package com.devsuj.crmdesktop.client;

import com.devsuj.crmdesktop.model.CambioEstadoProductoRequest;
import com.devsuj.crmdesktop.model.MovimientoInventario;
import com.devsuj.crmdesktop.model.MovimientoInventarioRequest;
import com.devsuj.crmdesktop.model.Pagina;
import com.devsuj.crmdesktop.model.Producto;
import com.devsuj.crmdesktop.model.ProductoRequest;
import tools.jackson.core.type.TypeReference;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** {@link ProductoApi} que llama a la API REST. */
public class ProductoApiHttp implements ProductoApi {

    private static final String RUTA = "/productos";
    private static final TypeReference<Pagina<Producto>> PAGINA = new TypeReference<>() {};
    private static final TypeReference<List<Producto>> LISTA = new TypeReference<>() {};
    private static final TypeReference<Producto> PRODUCTO = new TypeReference<>() {};
    private static final TypeReference<MovimientoInventario> MOVIMIENTO = new TypeReference<>() {};
    private static final TypeReference<Pagina<MovimientoInventario>> KARDEX = new TypeReference<>() {};

    private final ApiHttp api;

    public ProductoApiHttp(ApiHttp api) {
        this.api = api;
    }

    @Override
    public Pagina<Producto> buscar(String texto, Integer categoriaId, Boolean activo, Boolean canjeables,
                                   int pagina, int tamano) {
        Map<String, Object> parametros = new LinkedHashMap<>();
        parametros.put("q", texto);
        parametros.put("categoriaId", categoriaId);
        parametros.put("activo", activo);
        parametros.put("canjeables", canjeables);
        parametros.put("page", pagina);
        parametros.put("size", tamano);
        return api.get(RUTA, parametros, PAGINA);
    }

    @Override
    public List<Producto> stockBajo() {
        return api.get(RUTA + "/stock-bajo", Map.of(), LISTA);
    }

    @Override
    public Producto crear(ProductoRequest datos) {
        return api.post(RUTA, datos, PRODUCTO);
    }

    @Override
    public Producto actualizar(int id, ProductoRequest datos) {
        return api.put(RUTA + "/" + id, datos, PRODUCTO);
    }

    @Override
    public Producto cambiarEstado(int id, boolean activo) {
        return api.patch(RUTA + "/" + id + "/estado", new CambioEstadoProductoRequest(activo), PRODUCTO);
    }

    @Override
    public MovimientoInventario registrarMovimiento(int productoId, MovimientoInventarioRequest datos) {
        return api.post(RUTA + "/" + productoId + "/movimientos", datos, MOVIMIENTO);
    }

    @Override
    public Pagina<MovimientoInventario> movimientos(int productoId, int pagina, int tamano) {
        Map<String, Object> parametros = new LinkedHashMap<>();
        parametros.put("page", pagina);
        parametros.put("size", tamano);
        return api.get(RUTA + "/" + productoId + "/movimientos", parametros, KARDEX);
    }
}
