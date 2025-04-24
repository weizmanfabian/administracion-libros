package com.weiz.Biblioteca.infraestructure.adapter;

import com.weiz.Biblioteca.aplication.port.out.AutorPort;
import com.weiz.Biblioteca.domain.entities.AutorEntity;
import com.weiz.Biblioteca.domain.repositories.AutorRepository;
import com.weiz.Biblioteca.util.Exceptions.CustomException;
import com.weiz.Biblioteca.util.Exceptions.IdNotFoundException;
import com.weiz.Biblioteca.util.enums.StoredProcedure;
import com.weiz.Biblioteca.util.errors.DataAccessUtils;
import jakarta.transaction.Transactional;
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
public class AutorAdapter implements AutorPort {
    private static final String NAME_FUNCTION_READ_ALL = StoredProcedure.FN_GET_AUTORES.getName();
    private static final String NAME_PROCEDURE_CREATE = StoredProcedure.USP_AUTOR_INSERT.getName();
    private static final String NAME_PROCEDURE_UPDATE = StoredProcedure.USP_AUTOR_UPDATE.getName();
    private static final String NAME_PROCEDURE_DELETE = StoredProcedure.USP_AUTOR_DELETE.getName();
    private static final String ERROR_CREATING_AUTOR_MESSAGE = "Error al crear autor";
    private static final String ERROR_UPDATING_AUTOR_MESSAGE = "Error al actualizar autor";
    private static final String ERROR_DELETING_AUTOR_MESSAGE = "Error al eliminar autor";
    private static final String ERROR_FETCHING_AUTORES_MESSAGE = "Error al obtener la lista de autores";

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;


    private void printExecutingProcedure(String procedureName, Map<String, Object> params) {
        log.info("Ejecutando {} con parámetros {}", procedureName, params);
    }

    @Override
    public Set<AutorEntity> findAll() {
        return DataAccessUtils.executeWithErrorHandling(ERROR_FETCHING_AUTORES_MESSAGE, () -> {
            return jdbcTemplate.query(
                    "SELECT * FROM %S".formatted(NAME_FUNCTION_READ_ALL),
                    (rs, rowNum) -> new AutorEntity(
                            rs.getInt("autor_id"),
                            rs.getString("nombre"),
                            rs.getString("apellido"),
                            rs.getString("nacionalidad")
                    )
            ).stream().collect(Collectors.toSet());
        }, NAME_FUNCTION_READ_ALL);
    }

    @Override
    @Transactional
    public AutorEntity update(AutorEntity request, Integer id) {
        return DataAccessUtils.executeWithErrorHandling(ERROR_UPDATING_AUTOR_MESSAGE, () -> {
            MapSqlParameterSource parameters = new MapSqlParameterSource()
                    .addValue("p_autor_id", id)
                    .addValue("p_nombre", request.getNombre(), Types.VARCHAR)
                    .addValue("p_apellido", request.getApellido(), Types.VARCHAR)
                    .addValue("p_nacionalidad", request.getNacionalidad(), Types.VARCHAR);

            String sql = "CALL %s(:p_autor_id, :p_nombre, :p_apellido, :p_nacionalidad)".formatted(NAME_PROCEDURE_UPDATE);
            printExecutingProcedure(NAME_PROCEDURE_UPDATE, parameters.getValues());

            namedParameterJdbcTemplate.update(sql, parameters);
            return findById(id).orElseThrow(() -> new IdNotFoundException("Autor"));
        }, NAME_PROCEDURE_UPDATE);
    }


    @Override
    @Transactional
    public void delete(Integer id) {
        DataAccessUtils.executeWithErrorHandling(ERROR_DELETING_AUTOR_MESSAGE, () -> {
            jdbcTemplate.update("CALL %s(?)".formatted(NAME_PROCEDURE_DELETE), id);
            return null; //Void return
        }, NAME_PROCEDURE_DELETE);
    }

    @Override
    @Transactional
    public AutorEntity save(AutorEntity request) {
        return DataAccessUtils.executeWithErrorHandling(ERROR_CREATING_AUTOR_MESSAGE, () -> {
            MapSqlParameterSource parameters = new MapSqlParameterSource()
                    .addValue("p_nombre", request.getNombre())
                    .addValue("p_apellido", request.getApellido())
                    .addValue("p_nacionalidad", request.getNacionalidad())
                    .addValue("new_id", null, Types.INTEGER);

            String sql = "CALL %s(:p_nombre, :p_apellido, :p_nacionalidad, :new_id)".formatted(NAME_PROCEDURE_CREATE);
            printExecutingProcedure(NAME_PROCEDURE_CREATE, parameters.getValues());

            Integer newId = (Integer) namedParameterJdbcTemplate.queryForMap(sql, parameters).get("new_id");
            if (newId == null) {
                throw new CustomException(ERROR_CREATING_AUTOR_MESSAGE);
            }
            return findById(newId).orElseThrow(() -> new IdNotFoundException("Autor"));
        }, NAME_PROCEDURE_CREATE);
    }

    @Override
    public Optional<AutorEntity> findById(Integer id) {
        return DataAccessUtils.executeWithErrorHandling("Error al obtener autor por id", () -> {
            List<AutorEntity> autores = jdbcTemplate.query(
                    "SELECT * FROM autor WHERE autor_id = ?",
                    new Object[]{id},
                    (rs, rowNum) -> new AutorEntity(
                            rs.getInt("autor_id"),
                            rs.getString("nombre"),
                            rs.getString("apellido"),
                            rs.getString("nacionalidad")
                    )
            );
            return autores.isEmpty() ? Optional.empty() : Optional.of(autores.get(0));
        }, "findById");
    }
}
