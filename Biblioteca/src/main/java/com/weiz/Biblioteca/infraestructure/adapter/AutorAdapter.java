package com.weiz.Biblioteca.infraestructure.adapter;

import com.weiz.Biblioteca.api.responses.AutorResponse;
import com.weiz.Biblioteca.aplication.port.out.AutorPort;
import com.weiz.Biblioteca.domain.entities.AutorEntity;
import com.weiz.Biblioteca.domain.repositories.AutorRepository;
import com.weiz.Biblioteca.util.Exceptions.CustomException;
import com.weiz.Biblioteca.util.Exceptions.IdNotFoundException;
import jakarta.transaction.Transactional;
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
public class AutorAdapter implements AutorPort {
    private static final String NAME_PROCEDURE_READ_ALL = "fn_get_autores()";
    private static final String NAME_PROCEDURE_CREATE = "uspAutorInsert";
    private static final String NAME_PROCEDURE_UPDATE = "uspAutorUpdate";
    private static final String NAME_PROCEDURE_DELETE = "uspAutorDelete";
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

    @Override
    @Transactional
    public AutorEntity save(AutorEntity request) {
        try {
            // Configure parameters with OUT parameter
            MapSqlParameterSource parameters = new MapSqlParameterSource()
                    .addValue("p_nombre", request.getNombre())
                    .addValue("p_apellido", request.getApellido())
                    .addValue("p_nacionalidad", request.getNacionalidad())
                    .addValue("new_id", null, Types.INTEGER); // OUT parameter to capture new ID

            String sql = "CALL %s(:p_nombre, :p_apellido, :p_nacionalidad, :new_id)".formatted(NAME_PROCEDURE_CREATE);
            printExecutingProcedure(NAME_PROCEDURE_CREATE, parameters.getValues());

            // Execute the query and retrieve the new ID
            Integer newId = (Integer) namedParameterJdbcTemplate.queryForMap(sql, parameters).get("new_id");

            if (newId == null) {
                throw new CustomException(ERROR_CREATING_AUTOR_MESSAGE);
            }
            return findById(newId).orElseThrow(() -> new IdNotFoundException("Autor"));
        } catch (DataAccessException e) {
            log.error("Error al ejecutar SP %s".formatted(NAME_PROCEDURE_CREATE), e);
            String errMsg = extractErrorMessage(e, ERROR_CREATING_AUTOR_MESSAGE);
            log.error("errMsg {}", errMsg);
            throw new CustomException(errMsg);
        }
    }

    @Override
    public Optional<AutorEntity> findById(Integer id) {
        var autor = autorRepository.findById(id)
                .orElseThrow(() -> new IdNotFoundException("Autor"));
        return Optional.of(autor);
    }

    @Override
    public Set<AutorEntity> findAll() {
        try {
            return new HashSet<>(jdbcTemplate.query("Select * from " + NAME_PROCEDURE_READ_ALL,
                    (rs, rowNum) -> AutorEntity.builder()
                            .id(rs.getInt("autor_id"))
                            .nombre(rs.getString("nombre"))
                            .apellido(rs.getString("apellido"))
                            .nacionalidad(rs.getString("nacionalidad"))
                            .build()
            ));
        } catch (DataAccessException e) {
            log.error("Error al ejecutar el SP %s".formatted(NAME_PROCEDURE_READ_ALL), e);
            throw new CustomException(ERROR_FETCHING_AUTORES_MESSAGE, e);
        }
    }

    @Override
    @Transactional
    public AutorEntity update(AutorEntity request, Integer id) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource()
                    .addValue("p_autor_id", id)
                    .addValue("p_nombre", request.getNombre())
                    .addValue("p_apellido", request.getApellido())
                    .addValue("p_nacionalidad", request.getNacionalidad());

            String sql = "CALL %s(:p_autor_id, :p_nombre, :p_apellido, :p_nacionalidad)".formatted(NAME_PROCEDURE_UPDATE);
            printExecutingProcedure(NAME_PROCEDURE_UPDATE, parameters.getValues());

            namedParameterJdbcTemplate.update(sql, parameters);
            return findById(id).orElseThrow(() -> new IdNotFoundException("Autor"));
        } catch (DataAccessException e) {
            log.error("Error al ejecutar SP %s".formatted(NAME_PROCEDURE_UPDATE), e);
            String errMsg = extractErrorMessage(e, ERROR_UPDATING_AUTOR_MESSAGE);
            log.error("errMsg {}", errMsg);
            throw new CustomException(errMsg);
        }
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        try {
            jdbcTemplate.update("CALL %s(?)".formatted(NAME_PROCEDURE_DELETE), id);
        } catch (DataAccessException e) {
            log.error("Error al ejecutar SP %s".formatted(NAME_PROCEDURE_DELETE), e);
            throw new CustomException(extractErrorMessage(e, ERROR_DELETING_AUTOR_MESSAGE));
        }
    }
}
