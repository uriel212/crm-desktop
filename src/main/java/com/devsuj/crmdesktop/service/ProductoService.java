package com.devsuj.crmdesktop.service;

import com.devsuj.crmdesktop.client.CategoriaApi;
import com.devsuj.crmdesktop.client.ProductoApi;
import com.devsuj.crmdesktop.model.Categoria;
import com.devsuj.crmdesktop.model.CategoriaRequest;
import com.devsuj.crmdesktop.model.MovimientoInventario;
import com.devsuj.crmdesktop.model.MovimientoInventarioRequest;
import com.devsuj.crmdesktop.model.Pagina;
import com.devsuj.crmdesktop.model.Producto;
import com.devsuj.crmdesktop.model.ProductoRequest;
import com.devsuj.crmdesktop.model.TipoMovimientoInventario;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Lógica del catálogo y el inventario: productos, categorías, ajustes manuales y kardex. Sin JavaFX.
 *
 * <p>El stock no se edita en el formulario: solo cambia con movimientos (ENTRADA/SALIDA manuales;
 * VENTA y CANJE los registra la API).</p>
 */
public class ProductoService {

    public static final int TAMANO_PAGINA = 20;
    public static final int TAMANO_KARDEX = 15;
    public static final String CATEGORIA = "categoriaId";
    public static final String NOMBRE = "nombre";
    public static final String DESCRIPCION = "descripcion";
    public static final String PRECIO = "precio";
    public static final String PRECIO_PUNTOS = "precioPuntos";
    public static final String STOCK_MINIMO = "stockMinimo";
    public static final String STOCK_INICIAL = "stockInicial";
    public static final String TIPO = "tipo";
    public static final String CANTIDAD = "cantidad";
    public static final String MOTIVO = "motivo";

    private final ProductoApi api;
    private final CategoriaApi categorias;

    public ProductoService(ProductoApi api, CategoriaApi categorias) {
        this.api = api;
        this.categorias = categorias;
    }

    public Pagina<Producto> buscar(String texto, Categoria categoria, Boolean activo, int pagina) {
        return api.buscar(Reglas.limpiar(texto), categoria == null ? null : categoria.id(), activo, null,
                pagina, TAMANO_PAGINA);
    }

    /** Productos activos para elegir al vender (los primeros 100 por nombre). */
    public List<Producto> paraVenta() {
        return api.buscar(null, null, true, null, 0, 100).contenido();
    }

    /** Productos activos con precio en puntos (para canjes de fidelización). */
    public List<Producto> canjeables() {
        return api.buscar(null, null, true, true, 0, 100).contenido();
    }

    /** Productos que hay que resurtir (stock ≤ mínimo). Solo GERENTE y ADMIN. */
    public List<Producto> stockBajo() {
        return api.stockBajo();
    }

    public List<Categoria> categorias() {
        return categorias.listar().stream().sorted(Comparator.comparing(Categoria::nombre)).toList();
    }

    public Pagina<MovimientoInventario> kardex(Producto producto, int pagina) {
        return api.movimientos(producto.id(), pagina, TAMANO_KARDEX);
    }

    // ---------- Reglas (las mismas de ProductoRequest de la API) ----------

    public static Optional<String> reglaNombre(String valor) {
        return Reglas.obligatorio(valor, "Escribe el nombre del producto.").or(() -> Reglas.maximo(valor, 120));
    }

    public static Optional<String> reglaPrecio(String valor) {
        return Reglas.montoValido(valor, "Escribe el precio en quetzales.", 8);
    }

    public static Optional<String> reglaPuntos(String valor) {
        return Reglas.enteroValido(valor, false, 1, "");
    }

    public static Optional<String> reglaStockMinimo(String valor) {
        return Reglas.enteroValido(valor, true, 0, "Escribe el stock mínimo (0 si no aplica).");
    }

    public Producto guardar(Producto original, ProductoFormulario datos) {
        Map<String, String> errores = new LinkedHashMap<>();
        Reglas.revisar(errores, CATEGORIA, () -> Reglas.obligatorio(datos.categoria(), "Elige la categoría."));
        Reglas.revisar(errores, NOMBRE, () -> reglaNombre(datos.nombre()));
        Reglas.revisar(errores, DESCRIPCION, () -> Reglas.maximo(datos.descripcion(), 5000));
        Reglas.revisar(errores, PRECIO, () -> reglaPrecio(datos.precio()));
        Reglas.revisar(errores, PRECIO_PUNTOS, () -> reglaPuntos(datos.precioPuntos()));
        Reglas.revisar(errores, STOCK_MINIMO, () -> reglaStockMinimo(datos.stockMinimo()));
        if (original == null) {
            Reglas.revisar(errores, STOCK_INICIAL, () -> Reglas.enteroValido(datos.stockInicial(), false, 0, ""));
        }
        Reglas.exigir(errores);

        ProductoRequest request = new ProductoRequest(datos.categoria().id(), datos.nombre().strip(),
                Reglas.limpiar(datos.descripcion()), Reglas.monto(datos.precio()).orElseThrow(),
                Reglas.entero(datos.precioPuntos()).orElse(null), Reglas.entero(datos.stockMinimo()).orElseThrow(),
                original == null ? Reglas.entero(datos.stockInicial()).orElse(0) : null);
        return original == null ? api.crear(request) : api.actualizar(original.id(), request);
    }

    public Producto cambiarEstado(Producto producto, boolean activo) {
        return api.cambiarEstado(producto.id(), activo);
    }

    /** Ajuste manual del inventario: ENTRADA (llegó mercancía) o SALIDA (merma, conteo físico). */
    public MovimientoInventario registrarMovimiento(Producto producto, TipoMovimientoInventario tipo,
                                                    String cantidad, String motivo) {
        Map<String, String> errores = new LinkedHashMap<>();
        if (tipo != TipoMovimientoInventario.ENTRADA && tipo != TipoMovimientoInventario.SALIDA) {
            errores.put(TIPO, "Elige Entrada o Salida.");
        }
        Reglas.revisar(errores, CANTIDAD, () -> Reglas.enteroValido(cantidad, true, 1, "Escribe la cantidad."));
        Reglas.revisar(errores, MOTIVO, () -> Reglas.obligatorio(motivo,
                "Escribe el motivo, por ejemplo \"Factura de proveedor F-1234\".").or(() -> Reglas.maximo(motivo, 150)));
        if (!errores.containsKey(CANTIDAD) && tipo == TipoMovimientoInventario.SALIDA
                && Reglas.entero(cantidad).orElse(0) > producto.stock()) {
            errores.put(CANTIDAD, "Solo hay " + producto.stock() + " en existencia.");
        }
        Reglas.exigir(errores);
        return api.registrarMovimiento(producto.id(),
                new MovimientoInventarioRequest(tipo, Reglas.entero(cantidad).orElseThrow(), motivo.strip()));
    }

    // ---------- Categorías ----------

    public Categoria guardarCategoria(Categoria original, String nombre, String descripcion) {
        Map<String, String> errores = new LinkedHashMap<>();
        Reglas.revisar(errores, NOMBRE, () -> Reglas.obligatorio(nombre, "Escribe el nombre de la categoría.")
                .or(() -> Reglas.maximo(nombre, 80)));
        Reglas.revisar(errores, DESCRIPCION, () -> Reglas.maximo(descripcion, 255));
        Reglas.exigir(errores);
        CategoriaRequest request = new CategoriaRequest(nombre.strip(), Reglas.limpiar(descripcion));
        return original == null ? categorias.crear(request) : categorias.actualizar(original.id(), request);
    }

    public void eliminarCategoria(Categoria categoria) {
        categorias.eliminar(categoria.id());
    }
}
