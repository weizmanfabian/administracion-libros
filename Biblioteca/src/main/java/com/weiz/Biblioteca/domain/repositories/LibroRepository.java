package com.weiz.Biblioteca.domain.repositories;

import com.weiz.Biblioteca.domain.entities.LibroEntity;
import org.springframework.data.repository.CrudRepository;

public interface LibroRepository extends CrudRepository<LibroEntity, Integer> {
}
