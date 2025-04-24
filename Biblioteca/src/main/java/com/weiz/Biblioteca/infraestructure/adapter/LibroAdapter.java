package com.weiz.Biblioteca.infraestructure.adapter;

import com.weiz.Biblioteca.aplication.port.out.LibroPort;
import com.weiz.Biblioteca.domain.entities.AutorEntity;
import com.weiz.Biblioteca.domain.entities.LibroEntity;
import com.weiz.Biblioteca.domain.repositories.LibroRepository;
import com.weiz.Biblioteca.util.Exceptions.CustomException;
import com.weiz.Biblioteca.util.Exceptions.IdNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Types;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class LibroAdapter implements LibroPort {
    private static final String NAME_PROCEDURE_READ_ALL = "fn_get_libros()";
    private static final String NAME_PROCEDURE_CREATE = "uspLibroInsert";
    private static final String NAME_PROCEDURE_UPDATE = "uspLibroUpdate";
    private static final String NAME_PROCEDURE_DELETE = "uspLibroDelete";
    private static final String ERROR_CREATING_LIBRO_MESSAGE = "Error al crear libro";
    private static final String ERROR_UPDATING_LIBRO_MESSAGE = "Error al actualizar libro";
    private static final String ERROR_DELETING_LIBRO_MESSAGE = "Error al eliminar libro";
    private static final String ERROR_FETCHING_LIBROS_MESSAGE = "Error al obtener la lista de libros";

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final LibroRepository libroRepository;

    private String extractErrorMessage(DataAccessException e, String defaultMessage) {
        String message = e.getMostSpecificCause() != null && e.getMostSpecificCause().getMessage() != null
                ? e.getMostSpecificCause().getMessage()
                : defaultMessage;
        return switch (message) {
            case String s when s.contains("título") -> "El título es requerido";
            case String s when s.contains("año") -> "El año de publicación es requerido";
            case String s when s.contains("Autor no encontrado") -> "Autor no encontrado";
            case String s when s.contains("El autor es requerido") -> "El autor es requerido";
            case String s when s.contains("libro") -> "Libro no encontrado";
            default -> message;
        };
    }

    private void printExecutingProcedure(String procedureName, Map<String, Object> params) {
        log.info("Ejecutando {} con parámetros {}", procedureName, params);
    }

    @Override
    public LibroEntity save(LibroEntity libro) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource()
                    .addValue("l_titulo", libro.getTitulo())
                    .addValue("l_anio_publicacion", libro.getAnioPublicacion())
                    .addValue("l_autor_id", libro.getAutorEntity().getId())
                    .addValue("new_id", null, Types.INTEGER);

            String sql = "CALL %s(:l_titulo, :l_anio_publicacion, :l_autor_id, :new_id)".formatted(NAME_PROCEDURE_CREATE);
            printExecutingProcedure("uspLibroInsert", parameters.getValues());

            //Map<String, Object> result = namedParameterJdbcTemplate.queryForMap(sql, parameters);
            //Integer newId = (Integer) result.get("new_id");
            Integer newId = (Integer) namedParameterJdbcTemplate.queryForMap(sql, parameters).get("new_id");

            if (newId == null) {
                throw new CustomException(ERROR_CREATING_LIBRO_MESSAGE);
            }
            return findById(newId).orElseThrow(() -> new IdNotFoundException("Libro"));
        } catch (DataAccessException e) {
            log.error("Error al ejecutar SP %s".formatted(NAME_PROCEDURE_CREATE), e);
            String errMsg = extractErrorMessage(e, ERROR_CREATING_LIBRO_MESSAGE);
            log.error("errMsg {}", errMsg);
            throw new CustomException(errMsg);
        }
    }

    @Override
    public Optional<LibroEntity> findById(Integer id) {
        var libro = libroRepository.findById(id)
                .orElseThrow(() -> new IdNotFoundException("Libro"));
        return Optional.of(libro);
    }

    @Override
    public Set<LibroEntity> findAll() {
        try {
            return new HashSet<>(jdbcTemplate.query("SELECT l.libro_id, l.titulo, l.anio_publicacion, a.autor_id, a.nombre AS autor_nombre, a.apellido AS autor_apellido, a.nacionalidad AS autor_nacionalidad FROM %s l JOIN autor a ON l.autor_id = a.autor_id".formatted(NAME_PROCEDURE_READ_ALL),
                    (rs, rowNum) -> LibroEntity.builder()
                            .id(rs.getInt("libro_id"))
                            .titulo(rs.getString("titulo"))
                            .anioPublicacion(rs.getInt("anio_publicacion"))
                            .autorEntity(AutorEntity.builder()
                                    .id(rs.getInt("autor_id"))
                                    .nombre(rs.getString("autor_nombre"))
                                    .apellido(rs.getString("autor_apellido"))
                                    .nacionalidad(rs.getString("autor_nacionalidad"))
                                    .build())
                            .build()
            ));
        } catch (DataAccessException e) {
            log.error("Error al obtener libros from %s".formatted(NAME_PROCEDURE_READ_ALL), e);
            throw new CustomException(ERROR_FETCHING_LIBROS_MESSAGE, e);
        }
    }

    @Override
    public LibroEntity update(LibroEntity libro, Integer id) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource()
                    .addValue("l_libro_id", id)
                    .addValue("l_titulo", libro.getTitulo())
                    .addValue("l_anio_publicacion", libro.getAnioPublicacion())
                    .addValue("l_autor_id", libro.getAutorEntity().getId());

            String sql = "CALL %s(:l_libro_id, :l_titulo, :l_anio_publicacion, :l_autor_id)".formatted(NAME_PROCEDURE_UPDATE);
            printExecutingProcedure(NAME_PROCEDURE_UPDATE, parameters.getValues());

            namedParameterJdbcTemplate.update(sql, parameters);
            return findById(id).orElseThrow(() -> new IdNotFoundException("Libro"));
        } catch (DataAccessException e) {
            log.error("Error al ejecutar SP %s".formatted(NAME_PROCEDURE_UPDATE), e);
            String errorMessage = extractErrorMessage(e, ERROR_UPDATING_LIBRO_MESSAGE);
            throw new CustomException(errorMessage);
        }
    }

    @Override
    public void delete(Integer id) {
        try {
            jdbcTemplate.update("CALL %s(?)".formatted(NAME_PROCEDURE_DELETE), id);
        } catch (DataAccessException e) {
            log.error("Error al ejecutar SP %s".formatted(NAME_PROCEDURE_DELETE), e);
            throw new CustomException(extractErrorMessage(e, ERROR_DELETING_LIBRO_MESSAGE));
        }
    }
}
