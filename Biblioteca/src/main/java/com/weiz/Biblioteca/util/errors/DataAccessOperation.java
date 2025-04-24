package com.weiz.Biblioteca.util.errors;


@FunctionalInterface
public interface DataAccessOperation<T> {
    T execute() throws org.springframework.dao.DataAccessException;
}
