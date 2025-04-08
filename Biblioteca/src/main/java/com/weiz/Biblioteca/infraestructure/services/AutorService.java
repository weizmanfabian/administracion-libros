package com.weiz.Biblioteca.infraestructure.services;

import com.weiz.Biblioteca.api.requests.AutorRequest;
import com.weiz.Biblioteca.api.responses.AutorResponse;
import com.weiz.Biblioteca.domain.entities.AutorEntity;
import com.weiz.Biblioteca.domain.repositories.AutorRepository;
import com.weiz.Biblioteca.infraestructure.services.imp.IAutorService;
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
public class AutorService implements IAutorService {

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
            case String s when s.contains("autor") -> "Autor no encontrado";
            default -> message;
        };
    }

    private AutorResponse executeStoredProcedure(String procedureName, Map<String, Object> params, String defaultErrorMessage) {
        try {
            Integer id = 0;

            if (procedureName.equals("uspAutorInsert")) {
                // Configurar parámetros para uspAutorInsert con OUT parameter
                MapSqlParameterSource parameters = new MapSqlParameterSource(params)
                        .addValue("new_id", null, Types.INTEGER); // Parámetro OUT

                String sql = "CALL " + procedureName + "(:p_nombre, :p_apellido, :p_nacionalidad, :new_id)";
                log.info("Ejecutando {} con parámetros {}", procedureName, params);

                // Ejecutar el SP y obtener el resultado
                Map<String, Object> result = namedParameterJdbcTemplate.queryForMap(sql, parameters);
                id = (Integer) result.get("new_id");

                if (id == null) {
                    throw new IllegalStateException("No se recibió un ID generado desde uspAutorInsert");
                }

            } else if (procedureName.equals("uspAutorUpdate")) {
                // Configurar parámetros para uspAutorUpdate
                MapSqlParameterSource parameters = new MapSqlParameterSource(params);
                String sql = "CALL " + procedureName + "(:p_autor_id, :p_nombre, :p_apellido, :p_nacionalidad)";
                log.info("Ejecutando {} con parámetros {}", procedureName, params);

                // Ejecutar el SP y obtener el resultado
                namedParameterJdbcTemplate.update(sql, parameters);

                // Obtener el ID desde los parámetros
                id = (Integer) params.get("p_autor_id");
                if (id == null) {
                    throw new IllegalStateException("No se recibió un ID generado desde uspAutorUpdate");
                }
            }
            var autor = autorRepository.findById(id)
                    .orElseThrow(() -> new IdNotFoundException("Autor"));
            return entityToResponse(autor);
        } catch (DataAccessException e) {
            log.error("Error al ejecutar SP {}", procedureName, e);
            String errorMessage = extractErrorMessage(e, defaultErrorMessage);
            throw new CustomException(errorMessage);
        }
    }

    @Override
    public Set<AutorResponse> readAll() {
        try {
            List<AutorResponse> autores = jdbcTemplate.query(
                    "SELECT * FROM fn_get_autores()",
                    (rs, rowNum) -> AutorResponse.builder()
                            .id(rs.getInt("autor_id"))
                            .nombre(rs.getString("nombre"))
                            .apellido(rs.getString("apellido"))
                            .nacionalidad(rs.getString("nacionalidad"))
                            .build()
            );
            return new HashSet<>(autores);
        } catch (DataAccessException e) {
            log.error("Error al obtener autores", e);
            throw new RuntimeException("Error al obtener la lista de autores", e);
        }
    }

    @Override
    public AutorResponse create(AutorRequest request) {
        return executeStoredProcedure(
                "uspAutorInsert",
                Map.of(
                        "p_nombre", request.getNombre(),
                        "p_apellido", request.getApellido(),
                        "p_nacionalidad", request.getNacionalidad()
                ),
                "Error al crear autor"
        );
    }

    @Override
    public AutorResponse readById(Integer id) {
        var autor = autorRepository.findById(id).orElseThrow(() -> new IdNotFoundException("Autor"));
        return entityToResponse(autor);
    }

    @Override
    public AutorResponse update(AutorRequest request, Integer id) throws InvocationTargetException, IllegalAccessException {
        return executeStoredProcedure(
                "uspAutorUpdate",
                Map.of(
                        "p_autor_id", id,
                        "p_nombre", request.getNombre(),
                        "p_apellido", request.getApellido(),
                        "p_nacionalidad", request.getNacionalidad()
                ),
                "Error al actualizar autor"
        );
    }

    @Override
    public void delete(Integer id) {
        try {
            // Definir los parámetros del procedimiento almacenado
            SqlParameterValue autorIdParam = new SqlParameterValue(Types.INTEGER, id);

            // Ejecutar el procedimiento almacenado
            jdbcTemplate.update("CALL uspAutorDelete(?)", autorIdParam);

        } catch (DataAccessException e) {
            log.error("Error al ejecutar SP uspAutorDelete", e);
            String errorMessage = extractErrorMessage(e, "Error al eliminar Autor");
            throw new CustomException(errorMessage);
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

    public static AutorEntity requestToEntity(AutorRequest request) {
        return AutorEntity.builder()
                .nombre(request.getNombre())
                .apellido(request.getApellido())
                .nacionalidad(request.getNacionalidad())
                .build();
    }
}
