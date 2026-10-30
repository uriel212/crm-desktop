package com.devsuj.crmdesktop.view.fidelizacion;

import com.devsuj.crmdesktop.model.CanjeResponse;
import com.devsuj.crmdesktop.model.Cliente;
import com.devsuj.crmdesktop.model.Producto;
import com.devsuj.crmdesktop.model.SaldoPuntos;
import com.devsuj.crmdesktop.service.FidelizacionService;
import com.devsuj.crmdesktop.util.Formatos;
import com.devsuj.crmdesktop.view.comun.DialogoGuardar;
import com.devsuj.crmdesktop.view.comun.Formulario;
import com.devsuj.crmdesktop.view.comun.Selectores;
import javafx.beans.binding.Bindings;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Window;
import org.controlsfx.control.SearchableComboBox;

import java.util.List;
import java.util.Optional;

/** Canje de puntos por un producto con precio en puntos. El canje descuenta stock (kardex CANJE). */
public final class CanjeDialogo {

    private CanjeDialogo() {
    }

    public static Optional<CanjeResponse> mostrar(Window ventana, FidelizacionService service, Cliente cliente,
                                                  SaldoPuntos saldo, List<Producto> canjeables) {
        Formulario formulario = new Formulario();
        SearchableComboBox<Producto> producto = formulario.campoAncho("Producto *", FidelizacionService.PRODUCTO,
                Selectores.buscable(canjeables, "Escribe para buscar un producto canjeable", p ->
                        p.nombre() + " · " + Formatos.entero(p.precioPuntos()) + " pts · " + p.stock() + " disp."));
        TextField cantidad = formulario.campo("Cantidad *", FidelizacionService.CANTIDAD, new TextField("1"));
        formulario.regla(producto, true, () -> producto.getValue() == null ? Optional.of("Elige el producto.") : Optional.empty());

        // Vista previa (informativa): el cálculo definitivo lo hace la API.
        Label resumen = new Label();
        resumen.getStyleClass().add("text-bold");
        resumen.setWrapText(true);
        resumen.textProperty().bind(Bindings.createStringBinding(() -> {
            Optional<Long> necesarios = FidelizacionService.puntosNecesarios(producto.getValue(), cantidad.getText());
            String disponibles = "Saldo disponible: " + Formatos.entero(saldo.saldo()) + " puntos.";
            return necesarios.map(n -> disponibles + " Este canje usa " + Formatos.entero(n) + " puntos"
                    + (n > saldo.saldo() ? " (no le alcanzan)." : "; quedarían " + Formatos.entero(saldo.saldo() - n) + "."))
                    .orElse(disponibles);
        }, producto.valueProperty(), cantidad.textProperty()));
        formulario.nota(resumen);

        return DialogoGuardar.mostrar(ventana, "Canjear puntos", cliente.nombre(), formulario, "Canjear",
                () -> {
                    Producto elegido = producto.getValue();
                    String texto = cantidad.getText();
                    return () -> service.canjear(cliente, elegido, texto);
                });
    }
}
