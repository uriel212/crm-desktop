package com.devsuj.crmdesktop.client;

import com.devsuj.crmdesktop.model.LoginRequest;
import com.devsuj.crmdesktop.model.LoginResponse;
import com.devsuj.crmdesktop.model.Usuario;

/** Operaciones de {@code /api/auth}. */
public interface AuthApi {

    /** {@code POST /api/auth/login}. Con credenciales incorrectas lanza {@link ApiException} 401. */
    LoginResponse login(LoginRequest credenciales);

    /** {@code GET /api/auth/me}: el usuario dueño del token. */
    Usuario yo();
}
