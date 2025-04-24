package com.weiz.Biblioteca.aplication.port.in;

import com.weiz.Biblioteca.api.requests.LibroRequest;
import com.weiz.Biblioteca.api.responses.LibroResponse;

import java.util.Set;

/**
 * Este puerto define las operaciones que la lógica de negocio puede realizar.
 * Usa los DTOs (LibroRequest y LibroResponse) para comunicarse con el mundo exterior.
 */
public interface LibroUseCase {
    LibroResponse create(LibroRequest request);

    LibroResponse readById(Integer id);

    Set<LibroResponse> readAll();

    LibroResponse update(LibroRequest request, Integer id);

    void delete(Integer id);
}
