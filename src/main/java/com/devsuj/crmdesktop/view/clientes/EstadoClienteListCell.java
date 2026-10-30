package com.devsuj.crmdesktop.view.clientes;

import com.devsuj.crmdesktop.model.EstadoCliente;
import com.devsuj.crmdesktop.view.comun.EtiquetaListCell;

/**
 * Celda de los ComboBox de estado. El valor {@code null} se muestra con un texto propio
 * (por ejemplo "Todos los estados" en el filtro del listado).
 */
public class EstadoClienteListCell extends EtiquetaListCell<EstadoCliente> {

    public EstadoClienteListCell(String textoSinValor) {
        super(textoSinValor, EstadoCliente::etiqueta);
    }
}
