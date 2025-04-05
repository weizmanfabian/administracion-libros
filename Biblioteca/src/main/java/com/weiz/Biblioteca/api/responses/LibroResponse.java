package com.weiz.Biblioteca.api.responses;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
public class LibroResponse {
    private Integer id;
    private String titulo;
    private Integer anioPublicacion;
    private AutorResponse autor;

}
