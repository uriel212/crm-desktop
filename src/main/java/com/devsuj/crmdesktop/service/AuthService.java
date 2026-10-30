package com.devsuj.crmdesktop.service;

import com.devsuj.crmdesktop.client.AuthApi;
import com.devsuj.crmdesktop.client.Sesion;
import com.devsuj.crmdesktop.model.LoginRequest;
import com.devsuj.crmdesktop.model.LoginResponse;
import com.devsuj.crmdesktop.model.Usuario;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Inicio y cierre de sesión. Valida las credenciales antes de llamar a la API y guarda el token
 * en la {@link Sesion} compartida, de donde lo toma {@code ApiHttp} para cada petición.
 */
public class AuthService {

    public static final String EMAIL = "email";
    public static final String PASSWORD = "password";

    private final AuthApi api;
    private final Sesion sesion;

    public AuthService(AuthApi api, Sesion sesion) {
        this.api = api;
        this.sesion = sesion;
    }

    /**
     * @throws ValidacionException si falta el email o la contraseña (no se llama a la API)
     * @throws com.devsuj.crmdesktop.client.ApiException 401 si las credenciales son incorrectas
     */
    public Usuario iniciarSesion(String email, String password) {
        Map<String, String> errores = new LinkedHashMap<>();
        Reglas.revisar(errores, EMAIL, () -> Reglas.email(email, "Escribe tu email."));
        Reglas.revisar(errores, PASSWORD, () -> Reglas.obligatorio(password, "Escribe tu contraseña."));
        Reglas.exigir(errores);

        // La contraseña NO se limpia: los espacios podrían ser parte de ella.
        LoginResponse respuesta = api.login(new LoginRequest(email.strip(), password));
        sesion.iniciar(respuesta);
        return respuesta.usuario();
    }

    public void cerrarSesion() {
        sesion.cerrar();
    }

    public Usuario usuarioActual() {
        return sesion.usuario();
    }
}
