CREATE OR REPLACE PROCEDURE uspLibroInsert(
    IN l_titulo VARCHAR(255),
    IN l_anio_publicacion INTEGER,
    IN l_autor_id INTEGER,
    OUT new_id INTEGER
)
LANGUAGE plpgsql
AS $$
BEGIN
    IF l_titulo IS NULL OR TRIM(l_titulo) = '' THEN
        RAISE EXCEPTION 'El título es requerido';
    END IF;

    IF l_anio_publicacion IS NULL THEN
        RAISE EXCEPTION 'El año de publicación es requerido';
    END IF;

    IF l_autor_id IS NULL OR l_autor_id = 0 THEN
        RAISE EXCEPTION 'El autor es requerido';
    END IF;

    -- Verificar si el autor existe
    IF NOT EXISTS (SELECT 1 FROM autor WHERE autor_id = l_autor_id) THEN
        RAISE EXCEPTION 'El autor con ID % no existe', l_autor_id;
    END IF;

    INSERT INTO libros (titulo, anio_publicacion, autor_id)
    VALUES (l_titulo, l_anio_publicacion, l_autor_id)
    RETURNING libro_id INTO new_id;

EXCEPTION
    WHEN OTHERS THEN
        RAISE EXCEPTION 'Un error ha ocurrido en el SP: %', SQLERRM;
END;
$$;