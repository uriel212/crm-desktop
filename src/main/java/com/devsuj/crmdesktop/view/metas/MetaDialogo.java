package com.devsuj.crmdesktop.view.metas;

import com.devsuj.crmdesktop.model.MetaVenta;
import com.devsuj.crmdesktop.model.Usuario;
import com.devsuj.crmdesktop.service.MetaService;
import com.devsuj.crmdesktop.util.Formatos;
import com.devsuj.crmdesktop.view.comun.DialogoGuardar;
import com.devsuj.crmdesktop.view.comun.EtiquetaListCell;
import com.devsuj.crmdesktop.view.comun.Formulario;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Window;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

/** Diálogo para crear o editar la meta mensual de un vendedor (GERENTE y ADMIN). */
public final class MetaDialogo {

    private MetaDialogo() {
    }

    public static Optional<MetaVenta> mostrar(Window ventana, MetaService service, MetaVenta original,
                                              List<Usuario> vendedores, int anioSugerido, int mesSugerido) {
        Formulario formulario = new Formulario();
        ComboBox<Usuario> vendedor = formulario.campoAncho("Vendedor *", MetaService.VENDEDOR, new ComboBox<>());
        vendedor.getItems().setAll(vendedores);
        EtiquetaListCell.aplicar(vendedor, "Elige el vendedor", Usuario::nombre);
        ComboBox<Integer> mes = formulario.campo("Mes *", MetaService.MES, new ComboBox<>());
        mes.getItems().setAll(IntStream.rangeClosed(1, 12).boxed().toList());
        EtiquetaListCell.aplicar(mes, "Elige el mes", m -> MetaService.periodo(2000, m).replace(" 2000", ""));
        ComboBox<Integer> anio = formulario.campo("Año *", MetaService.ANIO, new ComboBox<>());
        int actual = Formatos.hoy().getYear();
        anio.getItems().setAll(IntStream.rangeClosed(actual - 1, actual + 1).boxed().toList());
        EtiquetaListCell.aplicar(anio, "Elige el año", String::valueOf);
        TextField monto = formulario.campoAncho("Monto objetivo (Q) *", MetaService.MONTO, new TextField());
        monto.setPromptText("65000.00");
        formulario.regla(vendedor, true, () -> vendedor.getValue() == null ? Optional.of("Elige el vendedor.") : Optional.empty());
        formulario.regla(monto, true, () -> MetaService.reglaMonto(monto.getText()));

        if (original != null) {
            vendedores.stream().filter(u -> u.id().equals(original.usuarioId())).findFirst().ifPresent(vendedor::setValue);
            mes.setValue(original.mes());
            if (!anio.getItems().contains(original.anio())) {
                anio.getItems().addFirst(original.anio());
            }
            anio.setValue(original.anio());
            monto.setText(original.montoObjetivo().toPlainString());
        } else {
            mes.setValue(mesSugerido);
            anio.setValue(anioSugerido);
        }

        return DialogoGuardar.mostrar(ventana, original == null ? "Nueva meta" : "Editar meta",
                original == null ? "Meta mensual de ventas" : original.usuarioNombre() + " · "
                        + MetaService.periodo(original.anio(), original.mes()),
                formulario, "Guardar",
                () -> {
                    Usuario elegido = vendedor.getValue();
                    Integer a = anio.getValue();
                    Integer m = mes.getValue();
                    String texto = monto.getText();
                    return () -> service.guardar(original, elegido, a, m, texto);
                });
    }
}
