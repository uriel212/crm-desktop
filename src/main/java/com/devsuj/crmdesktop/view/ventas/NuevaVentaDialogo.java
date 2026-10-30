package com.devsuj.crmdesktop.view.ventas;

import com.devsuj.crmdesktop.model.CalculoVenta;
import com.devsuj.crmdesktop.model.Cliente;
import com.devsuj.crmdesktop.model.DetalleVenta;
import com.devsuj.crmdesktop.model.Oportunidad;
import com.devsuj.crmdesktop.model.Producto;
import com.devsuj.crmdesktop.model.Usuario;
import com.devsuj.crmdesktop.model.Venta;
import com.devsuj.crmdesktop.service.CarritoVenta;
import com.devsuj.crmdesktop.service.OportunidadService;
import com.devsuj.crmdesktop.service.VentaService;
import com.devsuj.crmdesktop.util.Formatos;
import com.devsuj.crmdesktop.util.MensajesError;
import com.devsuj.crmdesktop.util.TareaEnSegundoPlano;
import com.devsuj.crmdesktop.view.comun.DialogoGuardar;
import com.devsuj.crmdesktop.view.comun.EtiquetaListCell;
import com.devsuj.crmdesktop.view.comun.Formulario;
import com.devsuj.crmdesktop.view.comun.Selectores;
import com.devsuj.crmdesktop.view.comun.Tablas;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import org.controlsfx.control.SearchableComboBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Registro de una venta: cliente, oportunidad opcional, productos y cantidades.
 *
 * <p>Los precios, descuentos y el total NO se calculan aquí: cada vez que cambia el carrito se
 * pide la cotización a la API ({@code POST /api/ventas/calculo}) y se muestra. Al confirmar, la API
 * vuelve a calcular y guarda. Así el total que ve el usuario es el mismo que queda registrado.</p>
 */
public final class NuevaVentaDialogo {

    private NuevaVentaDialogo() {
    }

    /**
     * @param clientes     clientes que el usuario puede gestionar
     * @param productos    productos activos
     * @param responsables vendedores y gerentes (solo GERENTE/ADMIN; vacío para VENDEDOR)
     */
    public static Optional<Venta> mostrar(Window ventana, VentaService service, OportunidadService oportunidades,
                                          List<Cliente> clientes, List<Producto> productos,
                                          List<Usuario> responsables) {
        CarritoVenta carrito = new CarritoVenta();
        Formulario formulario = new Formulario();
        formulario.ancho(820);

        SearchableComboBox<Cliente> cliente = formulario.campo("Cliente *", VentaService.CLIENTE,
                Selectores.clientes(clientes));
        ComboBox<Oportunidad> oportunidad = formulario.campo("Oportunidad que se gana", "oportunidadId", new ComboBox<>());
        EtiquetaListCell.aplicar(oportunidad, "Ninguna", o -> o.titulo() + " · " + Formatos.moneda(o.montoEstimado()));
        oportunidad.getItems().setAll((Oportunidad) null);
        ComboBox<Usuario> responsable = new ComboBox<>();
        if (!responsables.isEmpty()) {
            formulario.campo("Responsable de la venta", "usuarioId", responsable);
            responsable.getItems().add(null);
            responsable.getItems().addAll(responsables);
            EtiquetaListCell.aplicar(responsable, "Yo (quien la registra)", Usuario::nombreConRol);
        }
        formulario.regla(cliente, true, () -> cliente.getValue() == null ? Optional.of("Elige el cliente.") : Optional.empty());

        // Al elegir cliente se cargan sus oportunidades abiertas.
        cliente.valueProperty().addListener((obs, antes, elegido) -> {
            oportunidad.getItems().setAll((Oportunidad) null);
            oportunidad.setValue(null);
            if (elegido != null) {
                TareaEnSegundoPlano.ejecutar(() -> oportunidades.abiertasDe(elegido.id()),
                        lista -> {
                            if (elegido.equals(cliente.getValue())) {
                                oportunidad.getItems().addAll(lista);
                            }
                        },
                        error -> { /* la oportunidad es opcional: si falla, se puede vender sin ella */ },
                        null);
            }
        });

        SearchableComboBox<Producto> producto = formulario.campo("Producto", CarritoVenta.PRODUCTO,
                Selectores.productos(productos));
        TextField cantidad = new TextField("1");
        cantidad.setPrefColumnCount(5);
        Button agregar = new Button("Agregar", new FontIcon("fth-plus"));
        formulario.campoCon("Cantidad", CarritoVenta.CANTIDAD, cantidad, agregar);

        // Tabla de renglones con los montos que devuelve la API.
        TableView<DetalleVenta> tabla = new TableView<>();
        Tablas.ajustable(tabla);
        tabla.setPrefHeight(200);
        TableColumn<DetalleVenta, String> colProducto = new TableColumn<>("Producto");
        TableColumn<DetalleVenta, String> colCantidad = new TableColumn<>("Cant.");
        TableColumn<DetalleVenta, String> colPrecio = new TableColumn<>("Precio unit.");
        TableColumn<DetalleVenta, String> colDescuento = new TableColumn<>("Descuento");
        TableColumn<DetalleVenta, String> colPromo = new TableColumn<>("Promoción");
        TableColumn<DetalleVenta, String> colSubtotal = new TableColumn<>("Subtotal");
        Tablas.texto(colProducto, DetalleVenta::productoNombre);
        Tablas.numero(colCantidad, DetalleVenta::cantidad);
        Tablas.moneda(colPrecio, DetalleVenta::precioUnitario);
        Tablas.moneda(colDescuento, DetalleVenta::descuento);
        Tablas.texto(colPromo, DetalleVenta::promocionNombre);
        Tablas.moneda(colSubtotal, DetalleVenta::subtotal);
        colProducto.setMinWidth(180);
        tabla.getColumns().setAll(List.of(colProducto, colCantidad, colPrecio, colDescuento, colPromo, colSubtotal));
        tabla.setPlaceholder(new Label("Agrega productos para ver el total."));
        Button quitar = new Button("Quitar producto", new FontIcon("fth-trash-2"));
        quitar.getStyleClass().add("danger");
        quitar.disableProperty().bind(tabla.getSelectionModel().selectedItemProperty().isNull());

        Label subtotal = new Label();
        Label descuento = new Label();
        Label total = new Label();
        total.getStyleClass().addAll("title-3", "total-venta");
        BooleanProperty calculando = new SimpleBooleanProperty(false);
        ProgressIndicator indicador = new ProgressIndicator();
        indicador.setPrefSize(18, 18);
        indicador.visibleProperty().bind(calculando);
        GridPane totales = new GridPane();
        totales.setHgap(16);
        totales.setVgap(4);
        totales.addRow(0, new Label("Subtotal"), subtotal);
        totales.addRow(1, new Label("Descuento sobre el total"), descuento);
        totales.addRow(2, new Label("Total"), total);
        HBox pie = new HBox(16, quitar, indicador, new Region(), totales);
        HBox.setHgrow(pie.getChildren().get(2), Priority.ALWAYS);
        pie.setAlignment(Pos.CENTER_LEFT);
        VBox carritoVista = new VBox(8, tabla, pie);
        formulario.nota(carritoVista);
        Label nota = new Label("Los precios y promociones los calcula el servidor con las promociones vigentes hoy. "
                + "Al registrar se descuenta el inventario y el cliente gana 1 punto por cada Q100.");
        nota.getStyleClass().addAll("text-muted", "text-small");
        nota.setWrapText(true);
        formulario.nota(nota);

        int[] ultimaCotizacion = {0};
        Runnable cotizar = () -> {
            int solicitud = ++ultimaCotizacion[0];
            if (carrito.estaVacio()) {
                tabla.getItems().clear();
                subtotal.setText(Formatos.moneda(BigDecimal.ZERO));
                descuento.setText("—");
                total.setText(Formatos.moneda(BigDecimal.ZERO));
                return;
            }
            TareaEnSegundoPlano.ejecutar(() -> service.cotizar(carrito),
                    (CalculoVenta calculo) -> {
                        if (solicitud == ultimaCotizacion[0]) {
                            tabla.getItems().setAll(calculo.detalles());
                            subtotal.setText(Formatos.moneda(calculo.subtotal()));
                            descuento.setText(calculo.descuento() == null || calculo.descuento().signum() == 0 ? "—"
                                    : "−" + Formatos.moneda(calculo.descuento())
                                    + (calculo.promocionNombre() == null ? "" : " (" + calculo.promocionNombre() + ")"));
                            total.setText(Formatos.moneda(calculo.total()));
                        }
                    },
                    error -> {
                        if (solicitud == ultimaCotizacion[0]) {
                            formulario.mostrarError(new IllegalStateException(
                                    "No se pudo calcular el total. " + MensajesError.de(error)));
                        }
                    },
                    calculando);
        };
        cotizar.run();

        agregar.setOnAction(e -> {
            try {
                formulario.prepararEnvio();
                carrito.agregar(producto.getValue(), cantidad.getText());
                producto.setValue(null);
                cantidad.setText("1");
                producto.requestFocus();
                cotizar.run();
            } catch (RuntimeException error) {
                formulario.mostrarError(error);
            }
        });
        cantidad.setOnAction(e -> {
            e.consume(); // Enter en la cantidad = agregar (y no registrar la venta)
            agregar.fire();
        });
        quitar.setOnAction(e -> {
            DetalleVenta elegido = tabla.getSelectionModel().getSelectedItem();
            if (elegido != null) {
                carrito.quitar(elegido.productoId());
                cotizar.run();
            }
        });

        return DialogoGuardar.mostrar(ventana, "Nueva venta", "Registrar una venta concretada (pagada)",
                formulario, "Registrar venta",
                () -> {
                    Cliente elegido = cliente.getValue();
                    Oportunidad ligada = oportunidad.getValue();
                    Usuario quien = responsable.getValue();
                    return () -> service.registrar(elegido, ligada, quien, carrito);
                });
    }
}
