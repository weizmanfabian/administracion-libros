package com.weiz.Biblioteca.util.Exceptions;

import lombok.Getter;

@Getter
public class EntityNotFoundException extends RuntimeException{
    private final String entityName;
    private final Object id;

    public EntityNotFoundException(String entityName, Object id) {
        super(String.format("%s con ID %s no encontrado", entityName, id.toString()));
        this.entityName = entityName;
        this.id = id;
    }
}
