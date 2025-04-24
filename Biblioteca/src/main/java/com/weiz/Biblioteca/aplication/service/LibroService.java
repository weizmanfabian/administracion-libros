package com.weiz.Biblioteca.aplication.service;

import com.weiz.Biblioteca.api.requests.LibroRequest;
import com.weiz.Biblioteca.api.responses.AutorResponse;
import com.weiz.Biblioteca.api.responses.LibroResponse;
import com.weiz.Biblioteca.aplication.port.in.LibroUseCase;
import com.weiz.Biblioteca.aplication.port.out.LibroPort;
import com.weiz.Biblioteca.domain.entities.AutorEntity;
import com.weiz.Biblioteca.domain.entities.LibroEntity;
import com.weiz.Biblioteca.util.Exceptions.IdNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LibroService implements LibroUseCase {
    private final LibroPort libroPort;

    @Override
    @Transactional
    public LibroResponse create(LibroRequest request) {
        LibroEntity libroEntity = LibroService.requestToEntity(request);
        LibroEntity savedLibro = libroPort.save(libroEntity);
        return LibroService.entityToResponse(savedLibro);
    }

    @Override
    public LibroResponse readById(Integer id) {
        LibroEntity libroEntity = libroPort.findById(id)
                .orElseThrow(() -> new IdNotFoundException("Libro"));
        return LibroService.entityToResponse(libroEntity);
    }

    @Override
    public Set<LibroResponse> readAll() {
        return libroPort.findAll().stream()
                .map(LibroService::entityToResponse)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public LibroResponse update(LibroRequest request, Integer id) {
        LibroEntity libroEntity = LibroService.requestToEntity(request);
        libroEntity.setId(id);
        return LibroService.entityToResponse(libroPort.update(libroEntity));
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        libroPort.delete(id);
    }

    public static LibroResponse entityToResponse(LibroEntity libroEntity) {
        return LibroResponse.builder()
                .id(libroEntity.getId())
                .titulo(libroEntity.getTitulo())
                .anioPublicacion(libroEntity.getAnioPublicacion())
                .autor(AutorResponse.builder()
                        .id(libroEntity.getAutorEntity().getId())
                        .nombre(libroEntity.getAutorEntity().getNombre())
                        .apellido(libroEntity.getAutorEntity().getApellido())
                        .nacionalidad(libroEntity.getAutorEntity().getNacionalidad())
                        .build())
                .build();
    }

    public static LibroEntity requestToEntity(LibroRequest request) {
        return LibroEntity.builder()
                .titulo(request.getTitulo())
                .anioPublicacion(request.getAnioPublicacion())
                .autorEntity(AutorEntity.builder()
                        .id(request.getIdAutor())
                        .build())
                .build();
    }
}
