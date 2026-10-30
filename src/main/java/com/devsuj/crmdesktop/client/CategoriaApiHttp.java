package com.devsuj.crmdesktop.client;

import com.devsuj.crmdesktop.model.Categoria;
import com.devsuj.crmdesktop.model.CategoriaRequest;
import tools.jackson.core.type.TypeReference;

import java.util.List;
import java.util.Map;

/** {@link CategoriaApi} que llama a la API REST. */
public class CategoriaApiHttp implements CategoriaApi {

    private static final String RUTA = "/categorias";
    private static final TypeReference<List<Categoria>> LISTA = new TypeReference<>() {};
    private static final TypeReference<Categoria> CATEGORIA = new TypeReference<>() {};

    private final ApiHttp api;

    public CategoriaApiHttp(ApiHttp api) {
        this.api = api;
    }

    @Override
    public List<Categoria> listar() {
        return api.get(RUTA, Map.of(), LISTA);
    }

    @Override
    public Categoria crear(CategoriaRequest datos) {
        return api.post(RUTA, datos, CATEGORIA);
    }

    @Override
    public Categoria actualizar(int id, CategoriaRequest datos) {
        return api.put(RUTA + "/" + id, datos, CATEGORIA);
    }

    @Override
    public void eliminar(int id) {
        api.delete(RUTA + "/" + id);
    }
}
