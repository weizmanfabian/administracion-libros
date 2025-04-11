package com.weiz.Biblioteca.infraestructure.services;

import com.weiz.Biblioteca.api.requests.LibroRequest;
import com.weiz.Biblioteca.api.responses.LibroResponse;
import com.weiz.Biblioteca.domain.entities.AutorEntity;
import com.weiz.Biblioteca.domain.entities.LibroEntity;
import com.weiz.Biblioteca.domain.repositories.AutorRepository;
import com.weiz.Biblioteca.domain.repositories.LibroRepository;
import com.weiz.Biblioteca.infraestructure.services.imp.ILIbroService;
import com.weiz.Biblioteca.util.Exceptions.CustomException;
import com.weiz.Biblioteca.util.Exceptions.IdNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Types;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
@AllArgsConstructor
@Slf4j
public class LibroService implements ILIbroService {

    private static final String ERROR_CREATING_LIBRO_MESSAGE = "Error al crear libro";
    private static final String ERROR_UPDATING_LIBRO_MESSAGE = "Error al actualizar libro";
    private static final String ERROR_DELETING_LIBRO_MESSAGE = "Error al eliminar libro";
    private static final String ERROR_FETCHING_LIBROS_MESSAGE = "Error al obtener la lista de libros";

    private final LibroRepository libroRepository;
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;


    private String extractErrorMessage(DataAccessException e, String defaultMessage) {
        String message = e.getMostSpecificCause() != null && e.getMostSpecificCause().getMessage() != null
                ? e.getMostSpecificCause().getMessage()
                : defaultMessage;
        return switch (message) {
            case String s when s.contains("título") -> "El título es requerido";
            case String s when s.contains("año") -> "El año de publicación es requerido";
            case String s when s.contains("autor no encontrado") -> "Autor no encontrado";
            case String s when s.contains("El autor es requerido") -> "El autor es requerido";
            case String s when s.contains("libro") -> "Libro no encontrado";
            default -> message;
        };
    }

    private void printExecutingProcedure(String procedureName, Map<String, Object> params) {
        log.info("Ejecutando {} con parámetros {}", procedureName, params);
    }


    /**
     * Executes a stored procedure for managing book records in the database.
     * The stored procedure can either insert a new book or update an existing one.
     *
     * @param procedureName       The name of the stored procedure to execute.
     * @param params              A map of parameters to pass to the stored procedure.
     * @param defaultErrorMessage Default error message to use if an exception occurs.
     * @return A {@link LibroResponse} object representing the book entity after the operation.
     * @throws CustomException       If an error occurs during the stored procedure execution.
     * @throws IllegalStateException If the procedure name is unsupported or no ID is generated.
     */
    private LibroResponse executeStoredProcedure(String procedureName, Map<String, Object> params, String defaultErrorMessage) {
        try {
            // Use a switch expression to determine how to handle each stored procedure
            Integer id = switch (procedureName) {
                case "uspLibroInsert" -> {
                    // Configure parameters for uspLibroInsert with OUT parameter
                    MapSqlParameterSource parameters = new MapSqlParameterSource(params)
                            .addValue("new_id", null, Types.INTEGER); // OUT parameter to capture new ID
                    String sql = "CALL %s(:l_titulo, :l_anio_publicacion, :l_autor_id, :new_id)".formatted(procedureName);
                    printExecutingProcedure(procedureName, params);
                    // Execute the query and retrieve the new ID
                    yield (Integer) namedParameterJdbcTemplate.queryForMap(sql, parameters).get("new_id");
                }
                case "uspLibroUpdate" -> {
                    // Configure parameters for uspLibroUpdate
                    MapSqlParameterSource parameters = new MapSqlParameterSource(params);
                    String sql = "CALL %s(:l_libro_id, :l_titulo, :l_anio_publicacion, :l_autor_id)".formatted(procedureName);
                    printExecutingProcedure(procedureName, params);
                    // Execute the update operation
                    namedParameterJdbcTemplate.update(sql, parameters);
                    // Retrieve the ID from the parameters
                    yield (Integer) parameters.getValue("l_libro_id");
                }
                default ->
                        throw new IllegalStateException("Procedimiento almacenado no soportado {}".formatted(procedureName));
            };
            // Check if the ID is null and throw an exception if so
            if (id == null) {
                throw new IllegalStateException("No se recibió un ID generado desde {}".formatted(procedureName));
            }
            // Retrieve and return the book entity using the ID
            return libroRepository.findById(id)
                    .map(LibroService::entityToResponse)
                    .orElseThrow(() -> new IdNotFoundException("Libro"));
        } catch (DataAccessException e) {
            // Log the error and throw a custom exception with the extracted message
            log.error("Error al ejecutar SP {}", procedureName, e);
            String errMsg = extractErrorMessage(e,defaultErrorMessage);
            log.error("errMsg {}", errMsg);
            throw new CustomException(errMsg);
        }
    }

    @Override
    public Set<LibroResponse> readAll() {
        try {
            return new HashSet<>(jdbcTemplate.query("SELECT l.libro_id, l.titulo, l.anio_publicacion, a.autor_id, a.nombre AS autor_nombre, a.apellido AS autor_apellido, a.nacionalidad AS autor_nacionalidad FROM fn_get_libros() l JOIN autor a ON l.autor_id = a.autor_id",
                    (rs, rowNum) -> LibroResponse.builder()
                            .id(rs.getInt("libro_id"))
                            .titulo(rs.getString("titulo"))
                            .anioPublicacion(rs.getInt("anio_publicacion"))
                            .autor(AutorService.entityToResponse(
                                    new AutorEntity(
                                            rs.getInt("autor_id"),
                                            rs.getString("autor_nombre"),
                                            rs.getString("autor_apellido"),
                                            rs.getString("autor_nacionalidad")
                                    )
                            ))
                            .build()
            ));
        } catch (DataAccessException e) {
            log.error("Error al obtener libros", e);
            throw new CustomException(ERROR_FETCHING_LIBROS_MESSAGE, e);
        }
    }

    @Override
    @Transactional
    public LibroResponse create(LibroRequest request) {
        return executeStoredProcedure(
                "uspLibroInsert",
                Map.of(
                        "l_titulo", request.getTitulo(),
                        "l_anio_publicacion", request.getAnioPublicacion(),
                        "l_autor_id", request.getIdAutor()
                ),
                ERROR_CREATING_LIBRO_MESSAGE
        );
    }

    @Override
    public LibroResponse readById(Integer id) {
        return libroRepository.findById(id)
                .map(LibroService::entityToResponse)
                .orElseThrow(() -> new IdNotFoundException("Libro"));
    }

    @Override
    @Transactional
    public LibroResponse update(LibroRequest request, Integer id) {
        return executeStoredProcedure(
                "uspLibroUpdate",
                Map.of(
                        "l_libro_id", id,
                        "l_titulo", request.getTitulo(),
                        "l_anio_publicacion", request.getAnioPublicacion(),
                        "l_autor_id", request.getIdAutor()
                ),
                ERROR_UPDATING_LIBRO_MESSAGE
        );
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        try {
            jdbcTemplate.update("CALL uspLibroDelete(?)", id);
        } catch (DataAccessException e) {
            log.error("Error al ejecutar SP uspLibroDelete", e);
            throw new CustomException(extractErrorMessage(e, ERROR_DELETING_LIBRO_MESSAGE));
        }
    }

    public static LibroResponse entityToResponse(LibroEntity libro) {
        return LibroResponse.builder()
                .id(libro.getId())
                .titulo(libro.getTitulo())
                .anioPublicacion(libro.getAnioPublicacion())
                .autor(AutorService.entityToResponse(libro.getAutorEntity()))
                .build();

    }

}