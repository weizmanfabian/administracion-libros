package com.weiz.Biblioteca.aplication.service;

import com.weiz.Biblioteca.api.requests.AutorRequest;
import com.weiz.Biblioteca.api.responses.AutorResponse;
import com.weiz.Biblioteca.aplication.port.in.AutorUseCase;
import com.weiz.Biblioteca.aplication.port.out.AutorPort;
import com.weiz.Biblioteca.domain.entities.AutorEntity;
import com.weiz.Biblioteca.util.Exceptions.IdNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AutorService implements AutorUseCase {
    private final AutorPort autorPort;

    @Override
    @Transactional
    public AutorResponse create(AutorRequest request) {
        var autorEntity = AutorService.requestToEntity(request);
        var savedAutor = autorPort.save(autorEntity);
        return AutorService.entityToResponse(savedAutor);
    }

    @Override
    public AutorResponse readById(Integer id) {
        var autorEntity = autorPort.findById(id)
                .orElseThrow(() -> new IdNotFoundException("Autor"));
        return AutorService.entityToResponse(autorEntity);
    }

    @Override
    public Set<AutorResponse> readAll() {
        return autorPort.findAll().stream()
                .map(AutorService::entityToResponse)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public AutorResponse update(AutorRequest request, Integer id) {
        var autorEntity = AutorService.requestToEntity(request);
        return AutorService.entityToResponse(autorPort.update(autorEntity, id));
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        autorPort.delete(id);
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
