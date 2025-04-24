package com.weiz.Biblioteca.util;


import org.springframework.dao.DataAccessException;

//La clase es final para evitar que se extienda.
//El constructor privado con UnsupportedOperationException asegura que no se pueda instanciar (es una clase utilitaria).
public final class ErrorMessageExtractor {
    // Constructor privado para evitar instanciación
    private ErrorMessageExtractor() {
        throw new UnsupportedOperationException("Clase utilitaria, no debe ser instanciada.");
    }

    /**
     * Extrae y mapea el mensaje de error de una DataAccessException a un mensaje amigable.
     *
     * @param e              La excepción de acceso a datos que contiene el mensaje de error.
     * @param defaultMessage El mensaje por defecto a devolver si no se puede mapear el error.
     * @return El mensaje de error mapeado o el mensaje por defecto.
     */
    public static String extractErrorMessage(DataAccessException e, String defaultMessage) {
        // Obtener el mensaje de la causa más específica
        String rawMessage = e.getMostSpecificCause() != null && e.getMostSpecificCause().getMessage() != null
                ? e.getMostSpecificCause().getMessage()
                : defaultMessage;

        return switch (rawMessage) {
            //libros
            case String s when s.contains("título") -> "El título es requerido";
            case String s when s.contains("año") -> "El año de publicación es requerido";
            case String s when s.contains("Autor no encontrado") -> "Autor no encontrado";
            case String s when s.contains("El autor es requerido") -> "El autor es requerido";
            case String s when s.contains("libro") -> "Libro no encontrado";

            //autores
            case String s when s.contains("nombre") -> "El nombre es requerido";
            case String s when s.contains("apellido") -> "El apellido es requerido";
            case String s when s.contains("Autor") -> "Autor no encontrado";

            default -> defaultMessage;
        };
    }
}
