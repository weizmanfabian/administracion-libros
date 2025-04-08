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
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.sql.Types;
import java.util.*;

@Service
@AllArgsConstructor
@Transactional
@Slf4j
public class LibroService implements ILIbroService {

    private final LibroRepository libroRepository;
    private final AutorRepository autorRepository;
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    /**
     * Extracts a human-readable error message from a {@link DataAccessException}, falling back to the given
     * {@code defaultMessage} if no specific error message can be found.
     *
     * @param e the {@link DataAccessException} to extract the error message from
     * @param defaultMessage the default error message to fall back to if no specific error message can be found
     * @return the extracted error message, or the default error message if no specific error message can be found
     */
    private String extractErrorMessage(DataAccessException e, String defaultMessage) {
        String message = e.getMostSpecificCause() != null && e.getMostSpecificCause().getMessage() != null
                ? e.getMostSpecificCause().getMessage()
                : defaultMessage;
        return switch (message) {
            case String s when s.contains("título") -> "El título es requerido";
            case String s when s.contains("año") -> "El año de publicación es requerido";
            case String s when s.contains("autor") -> "Autor no encontrado";
            case String s when s.contains("libro") -> "Libro no encontrado";
            default -> message;
        };
    }

    /**
     * Executes a stored procedure for managing 'Libro' entities, either inserting or updating records
     * in the database, and returns a response containing the updated or inserted entity.
     *
     * @param procedureName the name of the stored procedure to execute, e.g., "uspLibroInsert" or "uspLibroUpdate"
     * @param params a map of parameters to pass to the stored procedure, including fields like title,
     *               publication year, author ID, and optionally, the book ID for updates
     * @param defaultErrorMessage the default error message to use if an error occurs during execution
     * @return a {@link LibroResponse} object representing the resulting 'Libro' entity after the operation
     * @throws CustomException if the stored procedure execution fails or if required parameters are missing
     */
    private LibroResponse executeStoredProcedure(String procedureName, Map<String, Object> params, String defaultErrorMessage) {
        try {
            Integer id = 0;

            if (procedureName.equals("uspLibroInsert")) {
                // Configurar parámetros para uspLibroInsert con OUT parameter
                MapSqlParameterSource parameters = new MapSqlParameterSource(params)
                        .addValue("new_id", null, Types.INTEGER); // Parámetro OUT

                String sql = "CALL " + procedureName + "(:l_titulo, :l_anio_publicacion, :l_autor_id, :new_id)";
                log.info("Ejecutando {} con parámetros {}", procedureName, params);

                // Ejecutar el SP y obtener el resultado
                Map<String, Object> result = namedParameterJdbcTemplate.queryForMap(sql, parameters);
                id = (Integer) result.get("new_id");

                if (id == null) {
                    throw new IllegalStateException("No se recibió un ID generado desde uspLibroInsert");
                }
            } else if(procedureName.equals("uspLibroUpdate")) {
                // Configurar parámetros para uspLibroUpdate
                MapSqlParameterSource parameters = new MapSqlParameterSource(params);
                String sql = "CALL " + procedureName + "(:l_libro_id, :l_titulo, :l_anio_publicacion, :l_autor_id)";
                log.info("Ejecutando {} con parámetros {}", procedureName, params);

                // Ejecutar el procedimiento almacenado
                namedParameterJdbcTemplate.update(sql, parameters);

                // Obtener el ID desde los parámetros
                id = (Integer) params.get("l_libro_id");
                if (id == null) {
                    throw new IllegalArgumentException("El ID del libro (l_libro_id) es requerido para operaciones que no generan ID");
                }
            }

            // Buscar la entidad completa usando el ID
            var libro = libroRepository.findById(id)
                    .orElseThrow(() -> new IdNotFoundException("Libro no encontrado tras operación"));
            return entityToResponse(libro);

        } catch (DataAccessException e) {
            log.error("Error al ejecutar SP {}", procedureName, e);
            String errorMessage = extractErrorMessage(e, defaultErrorMessage);
            throw new CustomException(errorMessage);
        }
    }

    /**
     * Retrieves all books from the database and returns them as a set of LibroResponse objects.
     * This method queries the fn_get_libros() function and joins the results with the autor table
     * to include author details in each response.
     *
     * @return a Set of LibroResponse objects representing the books in the database.
     * @throws CustomException if there is any issue accessing the database or retrieving the books.
     */
    @Override
    public Set<LibroResponse> readAll() {
        try {
            return new HashSet<>(jdbcTemplate.query(
                    "SELECT l.libro_id, l.titulo, l.anio_publicacion, \n" +
                            "a.autor_id, a.nombre AS autor_nombre, a.apellido AS autor_apellido, a.nacionalidad AS autor_nacionalidad \n" +
                            "FROM fn_get_libros() l JOIN autor a ON l.autor_id = a.autor_id",
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
            throw new CustomException("Error al obtener la lista de libros");
        }
    }

    /**
     * Creates a new book record in the database by executing the stored procedure "uspLibroInsert".
     * The method uses the provided {@link LibroRequest} object to extract the necessary parameters
     * for the stored procedure, such as title, publication year, and author ID.
     *
     * @param request A {@link LibroRequest} object containing the details of the book to be created.
     * @return A {@link LibroResponse} object representing the newly created book entity.
     * @throws CustomException If an error occurs during the stored procedure execution or if any
     *                         required parameters are missing.
     */
    @Override
    public LibroResponse create(LibroRequest request) {
        return executeStoredProcedure(
                "uspLibroInsert",
                Map.of(
                        "l_titulo", request.getTitulo(),
                        "l_anio_publicacion", request.getAnioPublicacion(),
                        "l_autor_id", request.getIdAutor()
                ),
                "Error al crear Libro");
    }

/**
 * Retrieves a book from the database by its ID and returns its details.
 *
 * @param id the ID of the book to retrieve
 * @return a {@link LibroResponse} object containing the book's details,
 *         including title, publication year, and author information
 * @throws IdNotFoundException if no book with the given ID is found
 */
    @Override
    public LibroResponse readById(Integer id) {
        var libro = libroRepository.findById(id).orElseThrow(() -> new IdNotFoundException("Libro"));
        return LibroResponse.builder()
                .id(libro.getId())
                .titulo(libro.getTitulo())
                .anioPublicacion(libro.getAnioPublicacion())
                .autor(AutorService.entityToResponse(Objects.requireNonNull(autorRepository.findById(libro.getAutorEntity().getId()).orElse(null))))
                .build();
    }

    /**
     * Updates a book record in the database by executing the stored procedure "uspLibroUpdate".
     * The method uses the provided {@link LibroRequest} object to extract the necessary parameters
     * for the stored procedure, such as title, publication year, and author ID.
     *
     * @param request A {@link LibroRequest} object containing the updated details of the book.
     * @param id The ID of the book to be updated.
     * @return A {@link LibroResponse} object representing the updated book entity.
     * @throws CustomException If an error occurs during the stored procedure execution or if any
     *                         required parameters are missing.
     */
    @Override
    public LibroResponse update(LibroRequest request, Integer id) {
        return executeStoredProcedure(
                "uspLibroUpdate",
                Map.of(
                        "l_libro_id", id,
                        "l_titulo", request.getTitulo(),
                        "l_anio_publicacion", request.getAnioPublicacion(),
                        "l_autor_id", request.getIdAutor()
                ),
                "Error al actualizar Libro"
        );
    }

/**
 * Deletes a book record from the database using the stored procedure "uspLibroDelete".
 * The method takes the ID of the book to be deleted and executes the stored procedure
 * to remove the corresponding record from the database.
 *
 * @param id the ID of the book to delete
 * @throws CustomException if an error occurs during the stored procedure execution
 *                         or if the book cannot be deleted
 */
    @Override
    public void delete(Integer id) {
        try {
            // Definir los parámetros del procedimiento almacenado
            SqlParameterValue libroIdParam = new SqlParameterValue(Types.INTEGER, id);

            // Ejecutar el procedimiento almacenado
            jdbcTemplate.update("CALL uspLibroDelete(?)", libroIdParam);

        } catch (DataAccessException e) {
            log.error("Error al ejecutar SP uspLibroDelete", e);
            String errorMessage = extractErrorMessage(e, "Error al eliminar Libro");
            throw new CustomException(errorMessage);
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


    //------------------------------------------- código de pruebas-------------------------------------------
    public Set<LibroResponse> readAll1() {
        try {
            List<LibroResponse> libros = jdbcTemplate.query(
                    "SELECT * FROM fn_get_libros()",
                    (rs, rowNum) -> LibroResponse.builder()
                            .id(rs.getInt("libro_id"))
                            .titulo(rs.getString("titulo"))
                            .anioPublicacion(rs.getInt("anio_publicacion"))
                            .autor(AutorService.entityToResponse(Objects.requireNonNull(autorRepository.findById(rs.getInt("autor_id")).orElse(null))))
                            .build()
            );
            return new HashSet<>(libros);
        } catch (DataAccessException e) {
            log.error("Error al obtener autores", e);
            throw new RuntimeException("Error al obtener la lista de autores", e);
        }
    }

    public String create1(LibroRequest request) {
        try {
            // Definir los parámetros del procedimiento almacenado
            SqlParameterValue tituloParam = new SqlParameterValue(Types.VARCHAR, request.getTitulo());
            SqlParameterValue anioPublicacionParam = new SqlParameterValue(Types.INTEGER, request.getAnioPublicacion());
            SqlParameterValue autorIdParam = new SqlParameterValue(Types.INTEGER, request.getIdAutor());

            log.info("Parametros: " + request.toString());

            // Ejecutar el procedimiento almacenado
            jdbcTemplate.update("CALL uspLibroInsert(?, ?, ?)",
                    tituloParam, anioPublicacionParam, autorIdParam);

            return "Libro creado correctamente";

        } catch (DataAccessException e) {
            log.error("Error al ejecutar SP uspLibroInsert", e);
            String errorMessage = "Error al crear Libro";
            if (e.getMostSpecificCause() != null) {
                errorMessage = e.getMostSpecificCause().getMessage();
                if (errorMessage.contains("El título es requerido")) {
                    errorMessage = "El título es requerido";
                } else if (errorMessage.contains("El año de publicación es requerido")) {
                    errorMessage = "El año de publicación es requerido";
                } else if (errorMessage.contains("El autor con ID")) {
                    errorMessage = "Autor no encontrado";
                }
            }
            throw new CustomException(errorMessage);
        }
    }

    public String update1(LibroRequest request, Integer id) throws InvocationTargetException, IllegalAccessException {
        try {
            // Definir los parámetros del procedimiento almacenado
            SqlParameterValue libroIdParam = new SqlParameterValue(Types.INTEGER, id);
            SqlParameterValue tituloParam = new SqlParameterValue(Types.VARCHAR, request.getTitulo());
            SqlParameterValue anioPublicacionParam = new SqlParameterValue(Types.INTEGER, request.getAnioPublicacion());
            SqlParameterValue autorIdParam = new SqlParameterValue(Types.INTEGER, request.getIdAutor());

            // Ejecutar el procedimiento almacenado
            jdbcTemplate.update("CALL uspLibroUpdate(?, ?, ?, ?)",
                    libroIdParam, tituloParam, anioPublicacionParam, autorIdParam);

            return "Libro actualizado correctamente";

        } catch (DataAccessException e) {
            log.error("Error al ejecutar SP uspLibroUpdate", e);
            String errorMessage = "Error al crear Libro";
            if (e.getMostSpecificCause() != null) {
                errorMessage = e.getMostSpecificCause().getMessage();
                if (errorMessage.contains("El parámetro título no")) {
                    errorMessage = "El título es requerido";
                } else if (errorMessage.contains("El autor no existe")) {
                    errorMessage = "Autor no encontrado";
                } else if (errorMessage.contains("El libro no existe")) {
                    errorMessage = "Libro no encontrado";
                }
            }
            throw new CustomException(errorMessage);
        }
    }

    private LibroResponse executeStoredProcedure1(String procedureName, Map<String, Object> params, String defaultErrorMessage) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource(params);
            String sql = "CALL " + procedureName + "(" +
                    String.join(",", params.keySet().stream().map(k -> ":" + k).toArray(String[]::new)) + ")";
            log.info("Ejecutando {} con parámetros {}", procedureName, params);

            // Ejecutar el procedimiento almacenado
            namedParameterJdbcTemplate.update(sql, parameters);

            // Determinar el ID del libro (para insert o update)
            Integer libroId;
            if (procedureName.equals("uspLibroInsert")) {
                // Para inserciones, obtener el ID generado por la secuencia
                libroId = jdbcTemplate.queryForObject(
                        "SELECT currval('libros_libro_id_seq')",
                        Integer.class
                );
            } else {
                // Para actualizaciones u otros casos, usar el ID proporcionado en los parámetros
                libroId = (Integer) params.get("l_libro_id");
                if (libroId == null) {
                    throw new IllegalArgumentException("El ID del libro es requerido para operaciones que no generan ID");
                }
            }

            // Buscar la entidad completa usando el ID
            var libro = libroRepository.findById(libroId)
                    .orElseThrow(() -> new IdNotFoundException("Libro no encontrado tras operación"));
            return entityToResponse(libro);

        } catch (DataAccessException e) {
            log.error("Error al ejecutar SP {}", procedureName, e);
            String errorMessage = extractErrorMessage(e, defaultErrorMessage);
            throw new CustomException(errorMessage);
        }
    }

}

