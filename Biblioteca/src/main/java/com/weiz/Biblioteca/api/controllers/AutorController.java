package com.weiz.Biblioteca.api.controllers;

import com.weiz.Biblioteca.api.requests.AutorRequest;
import com.weiz.Biblioteca.api.responses.AutorResponse;
import com.weiz.Biblioteca.infraestructure.services.imp.IAutorService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

@RestController
@RequestMapping(path = "autores")
@AllArgsConstructor
public class AutorController {

    private final IAutorService autorService;

    @GetMapping
    public ResponseEntity<Set<AutorResponse>> get() {
        var response = autorService.readAll();
        return response.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(response);
    }

    @GetMapping(path = "{id}")
    public ResponseEntity<AutorResponse> get(@PathVariable Integer id) {
        var response = autorService.readById(id);
        return ResponseEntity.ok(response);
    }


    @PostMapping
    public ResponseEntity<String> post(@Valid @RequestBody AutorRequest request) {
        var response = autorService.create(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping(path = "{id}")
    public ResponseEntity<String> put(@Valid @RequestBody AutorRequest request, @PathVariable Integer id) throws InvocationTargetException, IllegalAccessException {
        var response = autorService.update(request, id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping(path = "{id}")
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        autorService.delete(id);
        return ResponseEntity.ok("Autor eliminado correctamente");
    }

}
