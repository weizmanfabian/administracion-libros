package com.weiz.Biblioteca.infraestructure.services;

import com.weiz.Biblioteca.api.requests.LibroRequest;
import com.weiz.Biblioteca.api.responses.LibroResponse;
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
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.sql.Types;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@AllArgsConstructor
@Transactional
@Slf4j
public class LibroService implements ILIbroService {

    private final LibroRepository libroRepository;
    private final AutorRepository autorRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Set<LibroResponse> readAll() {
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

    @Override
    public String create(LibroRequest request) {
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

    @Override
    public LibroResponse readById(Integer id) {
        var libro = libroRepository.findById(id).orElseThrow(() -> new IdNotFoundException("Libro no encontrado"));
        return LibroResponse.builder()
                .id(libro.getId())
                .titulo(libro.getTitulo())
                .anioPublicacion(libro.getAnioPublicacion())
                .autor(AutorService.entityToResponse(Objects.requireNonNull(autorRepository.findById(libro.getAutorEntity().getId()).orElse(null))))
                .build();
    }

    @Override
    public String update(LibroRequest request, Integer id) throws InvocationTargetException, IllegalAccessException {
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
                } else if(errorMessage.contains("El libro no existe")) {
                    errorMessage = "Libro no encontrado";
                }
            }
            throw new CustomException(errorMessage);
        }
    }

    @Override
    public void delete(Integer id) {
        try {
            // Definir los parámetros del procedimiento almacenado
            SqlParameterValue libroIdParam = new SqlParameterValue(Types.INTEGER, id);

            // Ejecutar el procedimiento almacenado
            jdbcTemplate.update("CALL uspLibroDelete(?)", libroIdParam);

        } catch (DataAccessException e) {
            log.error("Error al ejecutar SP uspLibroDelete", e);
            String errorMessage = "Error al eliminar libro";
            if (e.getMostSpecificCause() != null) {
                errorMessage = e.getMostSpecificCause().getMessage();
                if (errorMessage.contains("El libro no existe")) {
                    errorMessage = "Libro no encontrado";
                }
            }
            throw new CustomException(errorMessage);
        }
    }
}
