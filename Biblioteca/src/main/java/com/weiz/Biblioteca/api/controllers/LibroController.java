package com.weiz.Biblioteca.api.controllers;

import com.weiz.Biblioteca.api.requests.LibroRequest;
import com.weiz.Biblioteca.api.responses.LibroResponse;
import com.weiz.Biblioteca.aplication.port.in.LibroUseCase;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

@RestController
@RequestMapping(path = "libros")
@AllArgsConstructor
public class LibroController {
    private final LibroUseCase libroUseCase;

    @GetMapping
    public ResponseEntity<Set<LibroResponse>> get() {
        var response = libroUseCase.readAll();
        return response.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(response);
    }

    @GetMapping(path = "{id}")
    public ResponseEntity<LibroResponse> get(@PathVariable Integer id) {
        var response = libroUseCase.readById(id);
        return response == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(response);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<LibroResponse> post(@Valid @RequestBody LibroRequest request) {
        var response = libroUseCase.create(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping(path = "{id}")
    public ResponseEntity<LibroResponse> put(@Valid @RequestBody LibroRequest request, @PathVariable Integer id) throws InvocationTargetException, IllegalAccessException {
        var response = libroUseCase.update(request, id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping(path = "{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        libroUseCase.delete(id);
        return ResponseEntity.ok("Libro eliminado correctamente");
    }
}
