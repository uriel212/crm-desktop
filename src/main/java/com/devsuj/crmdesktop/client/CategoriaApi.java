package com.devsuj.crmdesktop.client;

import com.devsuj.crmdesktop.model.Categoria;
import com.devsuj.crmdesktop.model.CategoriaRequest;

import java.util.List;

/** Operaciones de {@code /api/categorias} (crear/editar: GERENTE y ADMIN; eliminar: ADMIN). */
public interface CategoriaApi {

    List<Categoria> listar();

    Categoria crear(CategoriaRequest datos);

    Categoria actualizar(int id, CategoriaRequest datos);

    void eliminar(int id);
}
