package com.weiz.Biblioteca.aplication.port.out;

import com.weiz.Biblioteca.domain.entities.LibroEntity;

import java.util.Optional;
import java.util.Set;

/**
 * Este puerto define las operaciones que la lógica de negocio necesita para interactuar con la persistencia.
 * Usa LibroEntity porque la lógica de negocio trabaja con entidades del dominio, no con DTOs.
 */
public interface LibroPort {
    LibroEntity save(LibroEntity libro);
    Optional<LibroEntity> findById(Integer id);
    Set<LibroEntity> findAll();
    LibroEntity update(LibroEntity libro);
    void delete(Integer id);
}
