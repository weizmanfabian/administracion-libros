package com.weiz.Biblioteca.util.errors;

import com.weiz.Biblioteca.util.ErrorMessageExtractor;
import com.weiz.Biblioteca.util.Exceptions.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;

@Slf4j
public final class DataAccessUtils {
    // Constructor privado para evitar instanciación
    private DataAccessUtils() {
        throw new UnsupportedOperationException("Clase utilitaria, no debe ser instanciada");
    }

    /**
     * Ejecuta una operación de acceso a datos y maneja excepciones de forma centralizada.
     *
     * @param defaultErrorMessage Mensaje de error por defecto si no se puede mapear la excepción.
     * @param operation Operación de acceso a datos a ejecutar.
     * @param operationName Nombre de la operación para logging.
     * @param <T> Tipo de retorno de la operación.
     * @return Resultado de la operación.
     * @throws CustomException Si ocurre un error durante la ejecución.
     */
    public static <T> T executeWithErrorHandling(String defaultErrorMessage, DataAccessOperation<T> operation,
                                                 String operationName) {
        try {
            return operation.execute();
        } catch (DataAccessException e) {
            log.error("Error al ejecutar operación {}: {}", operationName, e.getMessage(), e);
            String errorMessage = ErrorMessageExtractor.extractErrorMessage(e, defaultErrorMessage);
            throw new CustomException(errorMessage);
        }
    }

}
