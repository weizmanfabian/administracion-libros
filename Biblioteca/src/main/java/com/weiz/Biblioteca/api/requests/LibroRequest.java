package com.weiz.Biblioteca.api.requests;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Year;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
public class LibroRequest {

    int current_year = Year.now().getValue();

    @NotBlank(message = "El título no puede estar vacío")
    @Size(max = 255, message = "El título no puede exceder los 255 caracteres")
    private String titulo;

    @NotNull(message = "El año de publicación es requerido")
    @Min(value = 100, message = "El año de publicación mínimo permitido es 100")
    @Max(value = 2025, message = "El año de publicación no puede ser futuro")
    private Integer anioPublicacion;

    @NotNull(message = "El ID del autor es requerido")
    @Min(value = 1, message = "El ID del autor debe ser mayor que 0")
    private Integer idAutor;
}