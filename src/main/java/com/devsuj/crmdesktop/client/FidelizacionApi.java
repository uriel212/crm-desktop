package com.devsuj.crmdesktop.client;

import com.devsuj.crmdesktop.model.CanjeRequest;
import com.devsuj.crmdesktop.model.CanjeResponse;
import com.devsuj.crmdesktop.model.MovimientoPuntos;
import com.devsuj.crmdesktop.model.Pagina;
import com.devsuj.crmdesktop.model.SaldoPuntos;

/** Operaciones de {@code /api/clientes/{clienteId}/puntos}. */
public interface FidelizacionApi {

    SaldoPuntos saldo(int clienteId);

    /** Estado de cuenta, del más reciente al más antiguo. */
    Pagina<MovimientoPuntos> movimientos(int clienteId, int pagina, int tamano);

    /** Canje de puntos por un producto (201). 409 si no alcanzan los puntos o el stock. */
    CanjeResponse canjear(int clienteId, CanjeRequest datos);
}
