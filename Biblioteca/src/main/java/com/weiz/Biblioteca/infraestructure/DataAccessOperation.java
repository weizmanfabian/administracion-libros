package com.weiz.Biblioteca.infraestructure;


@FunctionalInterface
public interface DataAccessOperation<T> {
    T execute() throws org.springframework.dao.DataAccessException;
}
