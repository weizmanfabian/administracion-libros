package com.weiz.Biblioteca.api.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "test")
public class TestController {
    @GetMapping
    public ResponseEntity<String> test(){
        return ResponseEntity.ok("Success!!");
    }
}
