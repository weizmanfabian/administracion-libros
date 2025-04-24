package com.weiz.Biblioteca.infraestructure.adapter;

import com.weiz.Biblioteca.aplication.port.out.LibroPort;
import com.weiz.Biblioteca.domain.entities.AutorEntity;
import com.weiz.Biblioteca.domain.entities.LibroEntity;
import com.weiz.Biblioteca.domain.repositories.LibroRepository;
import com.weiz.Biblioteca.util.Exceptions.CustomException;
import com.weiz.Biblioteca.util.Exceptions.IdNotFoundException;
import com.weiz.Biblioteca.util.enums.StoredProcedure;
import com.weiz.Biblioteca.util.errors.DataAccessUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class LibroAdapter implements LibroPort {
    private static final String NAME_PROCEDURE_READ_ALL = StoredProcedure.FN_GET_LIBROS.getName();
    private static final String NAME_PROCEDURE_CREATE = StoredProcedure.USP_LIBRO_CREATE.getName();
    private static final String NAME_PROCEDURE_UPDATE = StoredProcedure.USP_LIBRO_UPDATE.getName();
    private static final String NAME_PROCEDURE_DELETE = StoredProcedure.USP_LIBRO_DELETE.getName();
    private static final String ERROR_CREATING_LIBRO_MESSAGE = "Error al crear libro";
    private static final String ERROR_UPDATING_LIBRO_MESSAGE = "Error al actualizar libro";
    private static final String ERROR_DELETING_LIBRO_MESSAGE = "Error al eliminar libro";
    private static final String ERROR_FETCHING_LIBROS_MESSAGE = "Error al obtener la lista de libros";

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;


    private void printExecutingProcedure(String procedureName, Map<String, Object> params) {
        log.info("Ejecutando {} con parámetros {}", procedureName, params);
    }

    @Override
    public LibroEntity save(LibroEntity libro) {
        return DataAccessUtils.executeWithErrorHandling(ERROR_CREATING_LIBRO_MESSAGE, () -> {
            MapSqlParameterSource parameters = new MapSqlParameterSource()
                    .addValue("l_titulo", libro.getTitulo())
                    .addValue("l_anio_publicacion", libro.getAnioPublicacion())
                    .addValue("l_autor_id", libro.getAutorEntity().getId())
                    .addValue("new_id", null, Types.INTEGER);

            String sql = "CALL %s(:l_titulo, :l_anio_publicacion, :l_autor_id, :new_id)".formatted(NAME_PROCEDURE_CREATE);
            printExecutingProcedure(NAME_PROCEDURE_CREATE, parameters.getValues());

            Integer newId = (Integer) namedParameterJdbcTemplate.queryForMap(sql, parameters).get("new_id");
            if (newId == null) {
                throw new CustomException(ERROR_CREATING_LIBRO_MESSAGE);
            }
            return findById(newId).orElseThrow(() -> new IdNotFoundException("Libro"));
        }, NAME_PROCEDURE_CREATE);
    }


    @Override
    public Optional<LibroEntity> findById(Integer id) {
        return DataAccessUtils.executeWithErrorHandling("Error al obtener libro por id", () -> {
            List<LibroEntity> libros = jdbcTemplate.query(
                    "SELECT l.libro_id, l.titulo, l.anio_publicacion, a.autor_id, a.nombre AS autor_nombre, a.apellido AS autor_apellido, a.nacionalidad AS autor_nacionalidad FROM libros l JOIN autor a ON l.autor_id = a.autor_id WHERE l.libro_id = ?",
                    new Object[]{id},
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
            );
            return libros.isEmpty() ? Optional.empty() : Optional.of(libros.get(0));
        }, "findById");
    }

    @Override
    public Set<LibroEntity> findAll() {
        return DataAccessUtils.executeWithErrorHandling(ERROR_FETCHING_LIBROS_MESSAGE, () -> {
            return jdbcTemplate.query(
                    "SELECT l.libro_id, l.titulo, l.anio_publicacion, a.autor_id, a.nombre AS autor_nombre, a.apellido AS autor_apellido, a.nacionalidad AS autor_nacionalidad FROM %s l JOIN autor a ON l.autor_id = a.autor_id".formatted(NAME_PROCEDURE_READ_ALL),
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
            ).stream().collect(Collectors.toSet());
        }, NAME_PROCEDURE_READ_ALL);
    }

    @Override
    public LibroEntity update(LibroEntity libro, Integer id) {
        return DataAccessUtils.executeWithErrorHandling(ERROR_UPDATING_LIBRO_MESSAGE, () -> {
            MapSqlParameterSource parameters = new MapSqlParameterSource()
                    .addValue("l_libro_id", id)
                    .addValue("l_titulo", libro.getTitulo())
                    .addValue("l_anio_publicacion", libro.getAnioPublicacion())
                    .addValue("l_autor_id", libro.getAutorEntity().getId());

            String sql = "CALL %s(:l_libro_id, :l_titulo, :l_anio_publicacion, :l_autor_id)".formatted(NAME_PROCEDURE_UPDATE);
            printExecutingProcedure(NAME_PROCEDURE_UPDATE, parameters.getValues());

            namedParameterJdbcTemplate.update(sql, parameters);
            return findById(id).orElseThrow(() -> new IdNotFoundException("Libro"));
        }, NAME_PROCEDURE_UPDATE);
    }

    @Override
    public void delete(Integer id) {
        DataAccessUtils.executeWithErrorHandling(ERROR_DELETING_LIBRO_MESSAGE, () -> {
            jdbcTemplate.update("CALL %s(?)".formatted(NAME_PROCEDURE_DELETE), id);
            return null;
        }, NAME_PROCEDURE_DELETE);
    }
}
