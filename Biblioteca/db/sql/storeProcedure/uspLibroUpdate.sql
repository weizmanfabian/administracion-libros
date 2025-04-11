
CREATE OR REPLACE PROCEDURE uspLibroUpdate(
    IN l_libro_id INT,
    IN l_titulo VARCHAR(255),
    IN l_anio_publicacion INTEGER,
    IN l_autor_id INTEGER
)
LANGUAGE plpgsql
AS $$
BEGIN

    -- Validamos si existe el libro
    IF NOT EXISTS (SELECT 1 FROM libros WHERE libro_id = l_libro_id) THEN
        RAISE EXCEPTION 'Libro no encontrado.';
    END IF;

    -- Validamos si el autor existe
    IF l_autor_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM autor WHERE autor_id = l_autor_id) THEN
        RAISE EXCEPTION 'Autor no encontrado.';
    END IF;

    -- Validamos parámetros obligatorios
    IF l_titulo IS NULL OR TRIM(l_titulo) = '' THEN
        RAISE EXCEPTION 'El título es requerido';
    END IF;

    -- Actualizamos el libro
    UPDATE libros SET
        titulo = l_titulo,
		autor_id = l_autor_id,
        anio_publicacion = CASE
            					WHEN l_anio_publicacion IS NOT NULL AND l_anio_publicacion > 0
            					THEN l_anio_publicacion
            					ELSE anio_publicacion

        END
    WHERE libro_id = l_libro_id;

EXCEPTION
    WHEN OTHERS THEN
        RAISE EXCEPTION '%', SQLERRM;
END;
$$;