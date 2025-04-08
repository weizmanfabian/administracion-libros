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
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.sql.Types;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
@Transactional
@Slf4j
public class AutorService implements IAutorService {

    private final AutorRepository autorRepository;
    private final JdbcTemplate jdbcTemplate;

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
        try {
            // Definir los parámetros del procedimiento almacenado
            SqlParameterValue nombreParam = new SqlParameterValue(Types.VARCHAR, request.getNombre());
            SqlParameterValue apellidoParam = new SqlParameterValue(Types.VARCHAR, request.getApellido());
            SqlParameterValue nacionalidadParam = new SqlParameterValue(Types.VARCHAR, request.getNacionalidad());

            // Ejecutar el procedimiento almacenado
            jdbcTemplate.update("CALL uspAutorInsert(?, ?, ?)",
                    nombreParam, apellidoParam, nacionalidadParam);

            return new AutorResponse();

        } catch (DataAccessException e) {
            log.error("Error al ejecutar SP uspAutorInsert", e);
            String errorMessage = "Error al crear autor";
            if (e.getMostSpecificCause() != null) {
                errorMessage = e.getMostSpecificCause().getMessage();
                if (errorMessage.contains("El parámetro nombre no puede ser")) {
                    errorMessage = "El nombre es obligatorio";
                } else if (errorMessage.contains("El parámetro apellido no puede ser")) {
                    errorMessage = "El apellido es obligatorio";
                }
            }
            throw new CustomException(errorMessage);
        }
    }

    @Override
    public AutorResponse readById(Integer id) {
        var autor = autorRepository.findById(id).orElseThrow(() -> new IdNotFoundException("Autor"));
        return entityToResponse(autor);
    }

    @Override
    public AutorResponse update(AutorRequest request, Integer id) throws InvocationTargetException, IllegalAccessException {
        try {
            // Definir los parámetros del procedimiento almacenado
            SqlParameterValue autorIdParam = new SqlParameterValue(Types.INTEGER, id);
            SqlParameterValue nombreParam = new SqlParameterValue(Types.VARCHAR, request.getNombre());
            SqlParameterValue apellidoParam = new SqlParameterValue(Types.VARCHAR, request.getApellido());
            SqlParameterValue nacionalidadParam = new SqlParameterValue(Types.VARCHAR, request.getNacionalidad());

            // Ejecutar el procedimiento almacenado
            jdbcTemplate.update("CALL uspAutorUpdate(?, ?, ?, ?)",
                    autorIdParam, nombreParam, apellidoParam, nacionalidadParam);

            return new AutorResponse();

        } catch (DataAccessException e) {
            log.error("Error al ejecutar SP uspAutorUpdate", e);
            String errorMessage = "Error al crear autor";
            if (e.getMostSpecificCause() != null) {
                errorMessage = e.getMostSpecificCause().getMessage();
                if (errorMessage.contains("El autor con id")) {
                    errorMessage = "Autor no encontrado";
                } else if (errorMessage.contains("El parámetro nombre no puede ser")) {
                    errorMessage = "El nombre es obligatorio";
                } else if (errorMessage.contains("El parámetro apellido no puede ser")) {
                    errorMessage = "El apellido es obligatorio";
                }
            }
            throw new CustomException(errorMessage);
        }
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
            String errorMessage = "Error al eliminar autor";
            if (e.getMostSpecificCause() != null) {
                errorMessage = e.getMostSpecificCause().getMessage();
                if (errorMessage.contains("El autor con id")) {
                    errorMessage = "Autor no encontrado";
                }
            }
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
