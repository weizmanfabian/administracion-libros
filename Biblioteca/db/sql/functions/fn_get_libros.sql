CREATE OR REPLACE FUNCTION fn_get_libros()
RETURNS TABLE (
    libro_id INTEGER,
    titulo VARCHAR,
    anio_publicacion INTEGER,
    autor_id INTEGER
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT l.libro_id, l.titulo, l.anio_publicacion, l.autor_id
    FROM libros l;
EXCEPTION
    WHEN OTHERS THEN
        RAISE EXCEPTION 'Error al obtener libros: %', SQLERRM;
END;
$$;