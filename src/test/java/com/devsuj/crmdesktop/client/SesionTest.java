package com.devsuj.crmdesktop.client;

import com.devsuj.crmdesktop.model.LoginResponse;
import com.devsuj.crmdesktop.model.Rol;
import com.devsuj.crmdesktop.model.Usuario;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SesionTest {

    private static final Instant AHORA = Instant.parse("2026-10-21T16:00:00Z");
    private static final Usuario ANA = new Usuario(1, "Ana", "ana@test.com", Rol.ADMIN, null, null, true, null);

    @Test
    void entregaElTokenMientrasEstaVigente() {
        Sesion sesion = new Sesion(Clock.fixed(AHORA, ZoneOffset.UTC));
        sesion.iniciar(new LoginResponse("abc", "Bearer", AHORA.plusSeconds(60), ANA));

        assertEquals("abc", sesion.token().orElseThrow());
        assertEquals(ANA, sesion.usuario());
    }

    @Test
    void noEntregaUnTokenVencido() {
        Sesion sesion = new Sesion(Clock.fixed(AHORA, ZoneOffset.UTC));
        sesion.iniciar(new LoginResponse("abc", "Bearer", AHORA.minusSeconds(1), ANA));

        assertTrue(sesion.token().isEmpty());
    }

    @Test
    void cerrarAvisaUnaSolaVezConElMotivo() {
        Sesion sesion = new Sesion();
        List<Sesion.MotivoCierre> avisos = new ArrayList<>();
        sesion.alCerrar(avisos::add);
        sesion.iniciar(new LoginResponse("abc", "Bearer", Instant.now().plusSeconds(60), ANA));

        sesion.cerrar();
        sesion.expirar(); // ya estaba cerrada: no vuelve a avisar

        assertFalse(sesion.iniciada());
        assertTrue(sesion.token().isEmpty());
        assertEquals(List.of(Sesion.MotivoCierre.CERRADA_POR_USUARIO), avisos);
    }
}
