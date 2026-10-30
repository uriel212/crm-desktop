package com.devsuj.crmdesktop.view.promociones;

import com.devsuj.crmdesktop.model.AlcancePromocion;
import com.devsuj.crmdesktop.model.Campana;
import com.devsuj.crmdesktop.model.Producto;
import com.devsuj.crmdesktop.model.Promocion;
import com.devsuj.crmdesktop.model.TipoPromocion;
import com.devsuj.crmdesktop.service.PromocionFormulario;
import com.devsuj.crmdesktop.service.PromocionService;
import com.devsuj.crmdesktop.util.Formatos;
import com.devsuj.crmdesktop.view.comun.DialogoGuardar;
import com.devsuj.crmdesktop.view.comun.EtiquetaListCell;
import com.devsuj.crmdesktop.view.comun.Formulario;
import javafx.beans.binding.Bindings;
import javafx.scene.control.ComboBox;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Window;
import javafx.util.StringConverter;
import org.controlsfx.control.CheckListView;

import java.util.List;
import java.util.Optional;

/**
 * Diálogo para crear o editar una promoción (GERENTE y ADMIN). Los campos se adaptan al alcance:
 * PRODUCTO pide los productos; VENTA permite un monto mínimo de compra.
 */
public final class PromocionDialogo {

    private PromocionDialogo() {
    }

    /**
     * @param campanas  campañas no cerradas a las que se puede ligar
     * @param productos productos activos del catálogo
     */
    public static Optional<Promocion> mostrar(Window ventana, PromocionService service, Promocion original,
                                              List<Campana> campanas, List<Producto> productos) {
        Formulario formulario = new Formulario();
        formulario.ancho(620);
        TextField nombre = formulario.campoAncho("Nombre *", PromocionService.NOMBRE, new TextField());
        ComboBox<TipoPromocion> tipo = formulario.campo("Tipo de descuento *", PromocionService.TIPO, new ComboBox<>());
        tipo.getItems().setAll(TipoPromocion.values());
        EtiquetaListCell.aplicar(tipo, "Elige el tipo");
        TextField valor = formulario.campo("Valor *", PromocionService.VALOR, new TextField());
        ComboBox<AlcancePromocion> alcance = formulario.campo("Alcance *", PromocionService.ALCANCE, new ComboBox<>());
        alcance.getItems().setAll(AlcancePromocion.values());
        EtiquetaListCell.aplicar(alcance, "Elige el alcance");
        TextField montoMinimo = formulario.campo("Compra mínima (Q)", PromocionService.MONTO_MINIMO, new TextField());
        montoMinimo.setPromptText("Vacío = sin mínimo");
        DatePicker inicio = formulario.campo("Desde *", PromocionService.FECHA_INICIO, new DatePicker());
        DatePicker fin = formulario.campo("Hasta *", PromocionService.FECHA_FIN, new DatePicker());
        ComboBox<Campana> campana = formulario.campoAncho("Campaña (opcional)", "campanaId", new ComboBox<>());
        campana.getItems().add(null);
        campana.getItems().addAll(campanas);
        EtiquetaListCell.aplicar(campana, "Sin campaña: aplica siempre que esté activa y en fechas",
                c -> c.nombre() + " · " + c.estado().etiqueta());
        CheckListView<Producto> listaProductos = formulario.campoAncho("Productos incluidos *",
                PromocionService.PRODUCTOS, new CheckListView<>());
        listaProductos.getItems().setAll(productos);
        listaProductos.setPrefHeight(160);
        listaProductos.setCellFactory(lista -> new CheckBoxListCell<>(
                listaProductos::getItemBooleanProperty, new StringConverter<>() {
            @Override
            public String toString(Producto p) {
                return p == null ? "" : p.nombre() + " · " + Formatos.moneda(p.precio());
            }

            @Override
            public Producto fromString(String texto) {
                return null;
            }
        }));

        // Los campos que no aplican al alcance elegido se deshabilitan (la API los rechazaría).
        montoMinimo.disableProperty().bind(alcance.valueProperty().isNotEqualTo(AlcancePromocion.VENTA));
        listaProductos.disableProperty().bind(alcance.valueProperty().isNotEqualTo(AlcancePromocion.PRODUCTO));
        valor.promptTextProperty().bind(Bindings.when(
                tipo.valueProperty().isEqualTo(TipoPromocion.PORCENTAJE)).then("Ej. 10 (= 10 %)").otherwise("Ej. 500.00 (Q)"));
        formulario.regla(nombre, true, () -> nombre.getText().isBlank()
                ? Optional.of("Escribe el nombre de la promoción.") : Optional.empty());

        if (original != null) {
            nombre.setText(original.nombre());
            tipo.setValue(original.tipo());
            valor.setText(original.valor() == null ? "" : original.valor().toPlainString());
            alcance.setValue(original.alcance());
            montoMinimo.setText(original.montoMinimo() == null ? "" : original.montoMinimo().toPlainString());
            inicio.setValue(original.fechaInicio());
            fin.setValue(original.fechaFin());
            campana.setValue(campanas.stream().filter(c -> c.id().equals(original.campanaId())).findFirst().orElse(null));
            productos.stream()
                    .filter(p -> original.productos().stream().anyMatch(incluido -> incluido.id().equals(p.id())))
                    .forEach(p -> listaProductos.getCheckModel().check(p));
        } else {
            tipo.setValue(TipoPromocion.PORCENTAJE);
            alcance.setValue(AlcancePromocion.PRODUCTO);
            inicio.setValue(Formatos.hoy());
            fin.setValue(Formatos.hoy().plusMonths(1));
        }

        return DialogoGuardar.mostrar(ventana, original == null ? "Nueva promoción" : "Editar promoción",
                original == null ? "La aplica el servidor automáticamente al vender" : original.nombre(),
                formulario, "Guardar",
                () -> {
                    var datos = new PromocionFormulario(campana.getValue(), nombre.getText(), tipo.getValue(),
                            alcance.getValue(), valor.getText(), montoMinimo.getText(), inicio.getValue(),
                            fin.getValue(), List.copyOf(listaProductos.getCheckModel().getCheckedItems()));
                    return () -> service.guardar(original, datos);
                });
    }
}
