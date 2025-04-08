package com.weiz.Biblioteca.api.controllers;

import com.weiz.Biblioteca.api.requests.AutorRequest;
import com.weiz.Biblioteca.api.requests.LibroRequest;
import com.weiz.Biblioteca.api.responses.AutorResponse;
import com.weiz.Biblioteca.api.responses.LibroResponse;
import com.weiz.Biblioteca.infraestructure.services.imp.ILIbroService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

@RestController
@RequestMapping(path = "libros")
@AllArgsConstructor
public class LibroController {
    private final ILIbroService libroService;

    @GetMapping
    public ResponseEntity<Set<LibroResponse>> get() {
        var response = libroService.readAll();
        return response.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(response);
    }

    @GetMapping(path = "{id}")
    public ResponseEntity<LibroResponse> get(@PathVariable Integer id) {
        var response = libroService.readById(id);
        return response == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<LibroResponse> post(@Valid @RequestBody LibroRequest request) {
        var response = libroService.create(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping(path = "{id}")
    public ResponseEntity<LibroResponse> put(@Valid @RequestBody LibroRequest request, @PathVariable Integer id) throws InvocationTargetException, IllegalAccessException {
        var response = libroService.update(request, id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping(path = "{id}")
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        libroService.delete(id);
        return ResponseEntity.ok("Libro eliminado correctamente");
    }
}
