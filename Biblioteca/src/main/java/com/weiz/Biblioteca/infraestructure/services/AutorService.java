package com.weiz.Biblioteca.infraestructure.services;

import com.weiz.Biblioteca.api.requests.AutorRequest;
import com.weiz.Biblioteca.api.responses.AutorResponse;
import com.weiz.Biblioteca.domain.entities.AutorEntity;
import com.weiz.Biblioteca.domain.repositories.AutorRepository;
import com.weiz.Biblioteca.infraestructure.services.imp.IAutorService;
import com.weiz.Biblioteca.util.Exceptions.CustomException;
import com.weiz.Biblioteca.util.Exceptions.IdNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.sql.Types;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AutorService implements IAutorService {

    private static final String ERROR_CREATING_AUTOR_MESSAGE = "Error al crear autor";
    private static final String ERROR_UPDATING_AUTOR_MESSAGE = "Error al actualizar autor";
    private static final String ERROR_DELETING_AUTOR_MESSAGE = "Error al eliminar autor";
    private static final String ERROR_FETCHING_AUTORES_MESSAGE = "Error al obtener la lista de autores";

    private final AutorRepository autorRepository;
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private String extractErrorMessage(DataAccessException e, String defaultMessage) {
        String message = e.getMostSpecificCause() != null && e.getMostSpecificCause().getMessage() != null
                ? e.getMostSpecificCause().getMessage()
                : defaultMessage;
        return switch (message) {
            case String s when s.contains("nombre") -> "El nombre es requerido";
            case String s when s.contains("apellido") -> "El apellido es requerido";
            case String s when s.contains("Autor") -> "Autor no encontrado";
            default -> defaultMessage;
        };
    }

    private void printExecutingProcedure(String procedureName, Map<String, Object> params) {
        log.info("Ejecutando {} con parámetros {}", procedureName, params);
    }

    /**
     * Executes a stored procedure for managing author records in the database.
     * The stored procedure can either insert a new author or update an existing one.
     *
     * @param procedureName       The name of the stored procedure to execute.
     * @param params              A map of parameters to pass to the stored procedure.
     * @param defaultErrorMessage Default error message to use if an exception occurs.
     * @return An {@link AutorResponse} object representing the author entity after the operation.
     * @throws CustomException       If an error occurs during the stored procedure execution.
     * @throws IllegalStateException If the procedure name is unsupported or no ID is generated.
     */
    private AutorResponse executeStoredProcedure(String procedureName, Map<String, Object> params, String defaultErrorMessage) {
        try {
            // Use a switch expression to determine how to handle each stored procedure
            Integer id = switch (procedureName) {
                case "uspAutorInsert" -> {
                    // Configure parameters for uspAutorInsert with OUT parameter
                    MapSqlParameterSource parameters = new MapSqlParameterSource(params)
                            .addValue("new_id", null, Types.INTEGER); // OUT parameter to capture new ID
                    String sql = "CALL %s(:p_nombre, :p_apellido, :p_nacionalidad, :new_id)".formatted(procedureName);
                    printExecutingProcedure(procedureName, params);
                    // Execute the query and retrieve the new ID
                    yield (Integer) namedParameterJdbcTemplate.queryForMap(sql, parameters).get("new_id");
                }
                case "uspAutorUpdate" -> {
                    // Configure parameters for uspAutorUpdate
                    MapSqlParameterSource parameters = new MapSqlParameterSource(params);
                    String sql = "CALL %s(:p_autor_id, :p_nombre, :p_apellido, :p_nacionalidad)".formatted(procedureName);
                    printExecutingProcedure(procedureName, params);
                    // Execute the update operation
                    namedParameterJdbcTemplate.update(sql, parameters);
                    // Retrieve the ID from the parameters
                    yield (Integer) parameters.getValue("p_autor_id");
                }
                default ->
                    throw new IllegalStateException("Procedimiento almacenado no soportado {}".formatted(procedureName));
            };
            // Validate the retrieved ID
            if (id == null) {
                throw new IllegalStateException("No se recibió un ID generado desde {}".formatted(procedureName));
            }
            // Retrieve and return the AutorResponse using the ID
            return autorRepository.findById(id)
                    .map(AutorService::entityToResponse)
                    .orElseThrow(() -> new IdNotFoundException("Autor"));
        } catch (DataAccessException e) {
            log.error("Error al ejecutar SP {}", procedureName, e);
            // Extract and throw a custom exception with the error message
            throw new CustomException(extractErrorMessage(e, defaultErrorMessage));
        }
    }

    @Override
    public Set<AutorResponse> readAll() {
        try {
            return new HashSet<>(jdbcTemplate.query("Select * from fn_get_autores()",
                    (rs, rowNum) -> AutorResponse.builder()
                            .id(rs.getInt("autor_id"))
                            .nombre(rs.getString("nombre"))
                            .apellido(rs.getString("apellido"))
                            .nacionalidad(rs.getString("nacionalidad"))
                            .build()
            ));
        } catch (DataAccessException e) {
            log.error("Error al obtener autores", e);
            throw new CustomException(ERROR_FETCHING_AUTORES_MESSAGE, e);
        }
    }

    @Override
    @Transactional
    public AutorResponse create(AutorRequest request) {
        return executeStoredProcedure(
                "uspAutorInsert",
                Map.of(
                        "p_nombre", request.getNombre(),
                        "p_apellido", request.getApellido(),
                        "p_nacionalidad", request.getNacionalidad()
                ),
                ERROR_CREATING_AUTOR_MESSAGE
        );
    }

    @Override
    public AutorResponse readById(Integer id) {
        return autorRepository.findById(id)
                .map(AutorService::entityToResponse)
                .orElseThrow(() -> new IdNotFoundException("Autor"));

    }

    @Override
    @Transactional
    public AutorResponse update(AutorRequest request, Integer id) throws InvocationTargetException, IllegalAccessException {
        return executeStoredProcedure(
                "uspAutorUpdate",
                Map.of(
                        "p_autor_id", id,
                        "p_nombre", request.getNombre(),
                        "p_apellido", request.getApellido(),
                        "p_nacionalidad", request.getNacionalidad()
                ),
                ERROR_UPDATING_AUTOR_MESSAGE
        );
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        try {
            jdbcTemplate.update("CALL uspAutorDelete(?)", id);
        } catch (DataAccessException e) {
            log.error("Error al ejecutar SP uspAutorDelete", e);
            throw new CustomException(extractErrorMessage(e, ERROR_DELETING_AUTOR_MESSAGE));
        }
    }

    public static AutorResponse entityToResponse(AutorEntity autorEntity) {
        return AutorResponse.builder()
                .id(autorEntity.getId())
                .nombre(autorEntity.getNombre())
                .apellido(autorEntity.getApellido())
                .nacionalidad(autorEntity.getNacionalidad())
                .build();
    }
}
