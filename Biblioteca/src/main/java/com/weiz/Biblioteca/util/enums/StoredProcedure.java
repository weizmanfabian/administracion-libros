package com.weiz.Biblioteca.util.enums;

public enum StoredProcedure {
    FN_GET_AUTORES("fn_get_autores()"),
    USP_AUTOR_INSERT("uspAutorInsert"),
    USP_AUTOR_UPDATE("uspAutorUpdate"),
    USP_AUTOR_DELETE("uspAutorDelete"),

    FN_GET_LIBROS("fn_get_libros()"),
    USP_LIBRO_CREATE("uspLibroInsert"),
    USP_LIBRO_UPDATE("uspLibroUpdate"),
    USP_LIBRO_DELETE("uspLibroDelete");


    private final String name;

    StoredProcedure(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}