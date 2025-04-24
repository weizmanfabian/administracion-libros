package com.weiz.Biblioteca.aplication.port.out;

import com.weiz.Biblioteca.domain.entities.AutorEntity;

import java.util.Optional;
import java.util.Set;

public interface AutorPort {
    AutorEntity save(AutorEntity autor);

    Optional<AutorEntity> findById(Integer id);

    Set<AutorEntity> findAll();

    AutorEntity update(AutorEntity libro, Integer id);

    void delete(Integer id);
}
