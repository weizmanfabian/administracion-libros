package com.weiz.Biblioteca.api.responses;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
public class AutorResponse {
    private Integer id;
    private String nombre;
    private String apellido;
    private String nacionalidad;
}
