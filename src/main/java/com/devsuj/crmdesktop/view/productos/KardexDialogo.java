package com.devsuj.crmdesktop.view.productos;

import com.devsuj.crmdesktop.model.MovimientoInventario;
import com.devsuj.crmdesktop.model.Pagina;
import com.devsuj.crmdesktop.model.Producto;
import com.devsuj.crmdesktop.model.TipoMovimientoInventario;
import com.devsuj.crmdesktop.service.ProductoService;
import com.devsuj.crmdesktop.util.EstadoVacio;
import com.devsuj.crmdesktop.util.MensajesError;
import com.devsuj.crmdesktop.util.Recursos;
import com.devsuj.crmdesktop.util.TareaEnSegundoPlano;
import com.devsuj.crmdesktop.view.comun.Paginacion;
import com.devsuj.crmdesktop.view.comun.Tablas;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;
import java.util.function.IntConsumer;

/** Kardex de un producto: cada entrada y salida con el stock que quedó, del más reciente al más antiguo. */
public final class KardexDialogo {

    private KardexDialogo() {
    }

    public static void mostrar(Window ventana, ProductoService service, Producto producto) {
        TableView<MovimientoInventario> tabla = new TableView<>();
        Tablas.ajustable(tabla);
        TableColumn<MovimientoInventario, String> fecha = new TableColumn<>("Fecha");
        TableColumn<MovimientoInventario, TipoMovimientoInventario> tipo = new TableColumn<>("Tipo");
        TableColumn<MovimientoInventario, String> cantidad = new TableColumn<>("Cantidad");
        TableColumn<MovimientoInventario, String> stock = new TableColumn<>("Stock");
        TableColumn<MovimientoInventario, String> motivo = new TableColumn<>("Motivo");
        TableColumn<MovimientoInventario, String> usuario = new TableColumn<>("Usuario");
        Tablas.fechaHora(fecha, MovimientoInventario::fecha);
        Tablas.etiqueta(tipo, MovimientoInventario::tipo, "movimiento-");
        Tablas.numero(cantidad, m -> m.tipo() == TipoMovimientoInventario.ENTRADA ? m.cantidad() : -m.cantidad());
        Tablas.numero(stock, MovimientoInventario::stockResultante);
        Tablas.texto(motivo, m -> m.ventaId() != null && (m.motivo() == null || m.motivo().isBlank())
                ? "Venta #" + m.ventaId() : m.motivo());
        Tablas.texto(usuario, MovimientoInventario::usuarioNombre);
        fecha.setMinWidth(120);
        motivo.setMinWidth(180);
        tabla.getColumns().setAll(List.of(fecha, tipo, cantidad, stock, motivo, usuario));

        BooleanProperty cargando = new SimpleBooleanProperty(false);
        ProgressIndicator indicador = new ProgressIndicator();
        VBox capa = new VBox(indicador);
        capa.setAlignment(Pos.CENTER);
        capa.getStyleClass().add("capa-carga");
        capa.visibleProperty().bind(cargando);
        StackPane centro = new StackPane(tabla, capa);
        VBox.setVgrow(centro, Priority.ALWAYS);

        Button anterior = botonIcono("fth-chevron-left");
        Button siguiente = botonIcono("fth-chevron-right");
        Label etiqueta = new Label();
        etiqueta.getStyleClass().add("text-muted");
        HBox pie = new HBox(8, etiqueta, anterior, siguiente);
        pie.setAlignment(Pos.CENTER_RIGHT);

        Paginacion[] paginacion = new Paginacion[1];
        IntConsumer cargar = pagina -> TareaEnSegundoPlano.ejecutar(
                () -> service.kardex(producto, pagina),
                (Pagina<MovimientoInventario> resultado) -> {
                    tabla.getItems().setAll(resultado.contenido());
                    paginacion[0].mostrar(resultado);
                    tabla.setPlaceholder(EstadoVacio.crear("fth-list", "Este producto aún no tiene movimientos.", null, null));
                },
                error -> tabla.setPlaceholder(EstadoVacio.crear("fth-alert-triangle",
                        "No se pudo cargar el kardex. " + MensajesError.de(error), null, null)),
                cargando);
        paginacion[0] = new Paginacion(anterior, siguiente, etiqueta, cargando, cargar, "movimientos");

        VBox contenido = new VBox(12, centro, pie);
        contenido.setPrefSize(760, 460);
        Dialog<Void> dialogo = new Dialog<>();
        dialogo.initOwner(ventana);
        dialogo.setTitle("Kardex");
        dialogo.setHeaderText(producto.nombre() + " · existencias actuales: " + producto.stock());
        dialogo.setResizable(true);
        dialogo.getDialogPane().setContent(contenido);
        dialogo.getDialogPane().getStylesheets().add(Recursos.url("css/styles.css").toExternalForm());
        dialogo.getDialogPane().getButtonTypes().setAll(ButtonType.CLOSE);
        cargar.accept(0);
        dialogo.showAndWait();
    }

    private static Button botonIcono(String icono) {
        Button boton = new Button(null, new FontIcon(icono));
        boton.getStyleClass().add("button-icon");
        return boton;
    }
}
