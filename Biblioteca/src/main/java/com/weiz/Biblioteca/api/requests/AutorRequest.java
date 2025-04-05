package com.weiz.Biblioteca.api.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
public class AutorRequest {
    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(max = 255, message = "El nombre no puede exceder los 255 caracteres")
    private String nombre;
    @NotBlank(message = "El apellido no puede estar vacío")
    @Size(max = 255, message = "El apellido no puede exceder los 255 caracteres")
    private String apellido;
    private String nacionalidad;
}
