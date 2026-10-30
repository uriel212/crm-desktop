package com.devsuj.crmdesktop.client;

import com.devsuj.crmdesktop.model.CambioEstadoUsuarioRequest;
import com.devsuj.crmdesktop.model.CambioPasswordRequest;
import com.devsuj.crmdesktop.model.Pagina;
import com.devsuj.crmdesktop.model.Rol;
import com.devsuj.crmdesktop.model.Usuario;
import com.devsuj.crmdesktop.model.UsuarioActualizarRequest;
import com.devsuj.crmdesktop.model.UsuarioCrearRequest;
import tools.jackson.core.type.TypeReference;

import java.util.LinkedHashMap;
import java.util.Map;

/** {@link UsuarioApi} que llama a la API REST. */
public class UsuarioApiHttp implements UsuarioApi {

    private static final String RUTA = "/usuarios";
    private static final TypeReference<Pagina<Usuario>> PAGINA_DE_USUARIOS = new TypeReference<>() {};
    private static final TypeReference<Usuario> USUARIO = new TypeReference<>() {};

    private final ApiHttp api;

    public UsuarioApiHttp(ApiHttp api) {
        this.api = api;
    }

    @Override
    public Pagina<Usuario> buscar(String texto, Rol rol, Boolean activo, int pagina, int tamano) {
        Map<String, Object> parametros = new LinkedHashMap<>();
        parametros.put("q", texto);
        parametros.put("rol", rol);
        parametros.put("activo", activo);
        parametros.put("page", pagina);
        parametros.put("size", tamano);
        parametros.put("sort", "nombre");
        return api.get(RUTA, parametros, PAGINA_DE_USUARIOS);
    }

    @Override
    public Usuario crear(UsuarioCrearRequest datos) {
        return api.post(RUTA, datos, USUARIO);
    }

    @Override
    public Usuario actualizar(int id, UsuarioActualizarRequest datos) {
        return api.put(RUTA + "/" + id, datos, USUARIO);
    }

    @Override
    public Usuario cambiarEstado(int id, boolean activo, boolean reasignarClientes) {
        return api.patch(RUTA + "/" + id + "/estado",
                new CambioEstadoUsuarioRequest(activo, reasignarClientes), USUARIO);
    }

    @Override
    public void cambiarPassword(int id, String passwordActual, String passwordNueva) {
        api.patch(RUTA + "/" + id + "/password", new CambioPasswordRequest(passwordActual, passwordNueva), null);
    }
}
