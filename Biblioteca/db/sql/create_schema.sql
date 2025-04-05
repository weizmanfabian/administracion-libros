CREATE TABLE autor (
    autor_id SERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    apellido VARCHAR(255) NOT NULL,
    nacionalidad VARCHAR(100)
);

CREATE TABLE libros (
    libro_id SERIAL PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    anio_publicacion INT,
    autor_id INT,
    FOREIGN KEY (autor_id) REFERENCES autor(autor_id)
);