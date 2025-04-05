package com.weiz.Biblioteca.domain.entities;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@ToString
@Table(name = "libros")
public class LibroEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "libro_id")
    private Integer id;

    @Column(name = "titulo")
    private String titulo;

    @Column(name = "anio_publicacion")
    private Integer anioPublicacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id", referencedColumnName = "autor_id")
    private AutorEntity autorEntity;

}
