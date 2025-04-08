package com.weiz.Biblioteca.infraestructure.abstractService;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

public interface CrudService<Req, Res, Id> {
    Set<Res> readAll();

    Res create(Req request);

    Res readById(Id id);

    Res update(Req request, Id id) throws InvocationTargetException, IllegalAccessException;

    void delete(Id id);

}
