package com.weiz.Biblioteca.aplication.port.in;

import com.weiz.Biblioteca.api.requests.AutorRequest;
import com.weiz.Biblioteca.api.responses.AutorResponse;

import java.util.Set;

public interface AutorUseCase {
    AutorResponse create(AutorRequest request);

    AutorResponse readById(Integer id);

    Set<AutorResponse> readAll();

    AutorResponse update(AutorRequest request, Integer id);

    void delete(Integer id);
}
