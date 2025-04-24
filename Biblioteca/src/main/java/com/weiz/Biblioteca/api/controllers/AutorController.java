package com.weiz.Biblioteca.api.controllers;

import com.weiz.Biblioteca.api.requests.AutorRequest;
import com.weiz.Biblioteca.api.responses.AutorResponse;
import com.weiz.Biblioteca.aplication.port.in.AutorUseCase;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

@RestController
@RequestMapping(path = "autores")
@AllArgsConstructor
public class AutorController {

    private final AutorUseCase autorUseCase;

    @GetMapping
    public ResponseEntity<Set<AutorResponse>> get() {
        var response = autorUseCase.readAll();
        return response.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(response);
    }

    @GetMapping(path = "{id}")
    public ResponseEntity<AutorResponse> get(@PathVariable Integer id) {
        var response = autorUseCase.readById(id);
        return ResponseEntity.ok(response);
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<AutorResponse> post(@Valid @RequestBody AutorRequest request) {
        var response = autorUseCase.create(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping(path = "{id}")
    public ResponseEntity<AutorResponse> put(@Valid @RequestBody AutorRequest request, @PathVariable Integer id) throws InvocationTargetException, IllegalAccessException {
        var response = autorUseCase.update(request, id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping(path = "{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        autorUseCase.delete(id);
        return ResponseEntity.ok("Autor eliminado correctamente");
    }

}
