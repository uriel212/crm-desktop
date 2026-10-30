package com.devsuj.crmdesktop.client;

import com.devsuj.crmdesktop.model.LoginRequest;
import com.devsuj.crmdesktop.model.LoginResponse;
import com.devsuj.crmdesktop.model.Usuario;
import tools.jackson.core.type.TypeReference;

import java.util.Map;

/** {@link AuthApi} que llama a la API REST. */
public class AuthApiHttp implements AuthApi {

    private static final TypeReference<LoginResponse> LOGIN = new TypeReference<>() {};
    private static final TypeReference<Usuario> USUARIO = new TypeReference<>() {};

    private final ApiHttp api;

    public AuthApiHttp(ApiHttp api) {
        this.api = api;
    }

    @Override
    public LoginResponse login(LoginRequest credenciales) {
        return api.post("/auth/login", credenciales, LOGIN);
    }

    @Override
    public Usuario yo() {
        return api.get("/auth/me", Map.of(), USUARIO);
    }
}
