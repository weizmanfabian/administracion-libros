package com.weiz.Biblioteca.util.Exceptions;

public class IdNotFoundException extends RuntimeException {
    private static final String ERROR_MESSAGE = "Registro no existe en %s";

    public IdNotFoundException(String tableName) {
        super(String.format(ERROR_MESSAGE, tableName));
    }

}
