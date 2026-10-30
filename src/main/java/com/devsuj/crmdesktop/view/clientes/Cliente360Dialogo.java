package com.devsuj.crmdesktop.view.clientes;

import com.devsuj.crmdesktop.model.Cliente;
import com.devsuj.crmdesktop.model.ResumenCliente;
import com.devsuj.crmdesktop.model.VentaResumen;
import com.devsuj.crmdesktop.util.Formatos;
import com.devsuj.crmdesktop.util.Recursos;
import com.devsuj.crmdesktop.view.comun.Tablas;
import com.devsuj.crmdesktop.view.comun.Tarjetas;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.util.List;

/**
 * Vista 360 del cliente ({@code GET /api/clientes/{id}/resumen}): en una sola ventana, sus compras,
 * oportunidades, actividad, puntos, contratos, tickets y campañas. Solo lectura.
 */
public final class Cliente360Dialogo {

    private Cliente360Dialogo() {
    }

    public static void mostrar(Window ventana, ResumenCliente r) {
        Cliente c = r.cliente();
        Label datos = new Label(Formatos.textoOGuion(c.empresa()) + " · " + c.email() + " · "
                + Formatos.textoOGuion(c.telefono()) + "\nEstado: " + c.estado().etiqueta() + " · Vendedor: "
                + (c.vendedorNombre() == null ? "sin asignar" : c.vendedorNombre()));
        datos.getStyleClass().add("text-muted");
        datos.setWrapText(true);

        TilePane tarjetas = new TilePane(10, 10);
        tarjetas.setPrefTileWidth(210);
        tarjetas.setPrefColumns(3);
        tarjetas.getChildren().setAll(
                Tarjetas.kpi("fth-shopping-cart", "Compras", Formatos.moneda(r.totalComprado()),
                        r.numeroCompras() + " compra(s) · ticket promedio " + Formatos.moneda(r.ticketPromedio())),
                Tarjetas.kpi("fth-calendar", "Última compra",
                        r.ultimaCompra() == null ? "Nunca" : Formatos.fecha(r.ultimaCompra().toLocalDate()),
                        r.ultimaCompra() == null ? "aún no ha comprado" : Formatos.fechaHora(r.ultimaCompra())),
                Tarjetas.kpi("fth-target", "Pipeline abierto", Formatos.moneda(r.montoPipeline()),
                        r.oportunidadesAbiertas() + " oportunidad(es) abiertas"),
                Tarjetas.kpi("fth-message-square", "Última interacción",
                        r.ultimaInteraccion() == null ? "Ninguna" : Formatos.fecha(r.ultimaInteraccion().toLocalDate()),
                        r.actividadesProgramadas() + " actividad(es) programadas"),
                Tarjetas.kpi("fth-gift", "Puntos", Formatos.entero(r.saldoPuntos()), "saldo para canjear"),
                Tarjetas.kpi("fth-file-text", "Postventa", r.contratosVigentes() + " contrato(s)",
                        r.ticketsAbiertos() + " ticket(s) abiertos · " + r.campanas() + " campaña(s)"));

        TableView<VentaResumen> ventas = new TableView<>();
        Tablas.ajustable(ventas);
        TableColumn<VentaResumen, String> fecha = new TableColumn<>("Fecha");
        TableColumn<VentaResumen, String> numero = new TableColumn<>("Venta");
        TableColumn<VentaResumen, String> vendedor = new TableColumn<>("Vendedor");
        TableColumn<VentaResumen, String> total = new TableColumn<>("Total");
        Tablas.fechaHora(fecha, VentaResumen::fecha);
        Tablas.texto(numero, v -> "#" + v.id());
        Tablas.texto(vendedor, VentaResumen::usuarioNombre);
        Tablas.moneda(total, VentaResumen::total);
        ventas.getColumns().setAll(List.of(fecha, numero, vendedor, total));
        ventas.getItems().setAll(r.ultimasVentas());
        ventas.setPlaceholder(new Label("Sin compras registradas."));
        ventas.setPrefHeight(200);

        Label tituloVentas = new Label("Últimas compras");
        tituloVentas.getStyleClass().add("title-4");
        VBox contenido = new VBox(14, datos, tarjetas, tituloVentas, ventas);
        contenido.setPrefWidth(700);
        ScrollPane desplazable = new ScrollPane(contenido);
        desplazable.setFitToWidth(true);
        desplazable.setPrefViewportHeight(560);
        desplazable.getStyleClass().add("edge-to-edge");

        Dialog<Void> dialogo = new Dialog<>();
        dialogo.initOwner(ventana);
        dialogo.setTitle("Vista 360");
        dialogo.setHeaderText(c.nombre());
        dialogo.setResizable(true);
        dialogo.getDialogPane().setContent(desplazable);
        dialogo.getDialogPane().getStylesheets().add(Recursos.url("css/styles.css").toExternalForm());
        dialogo.getDialogPane().getButtonTypes().setAll(ButtonType.CLOSE);
        dialogo.showAndWait();
    }
}
