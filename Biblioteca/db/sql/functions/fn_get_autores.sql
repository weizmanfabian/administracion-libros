CREATE OR REPLACE FUNCTION fn_get_autores()
RETURNS TABLE (
    autor_id INTEGER,
    nombre VARCHAR,
    apellido VARCHAR,
    nacionalidad VARCHAR
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT a.autor_id, a.nombre, a.apellido, a.nacionalidad
    FROM autor a;
EXCEPTION
    WHEN OTHERS THEN
        RAISE EXCEPTION 'Error al obtener autores: %', SQLERRM;
END;
$$;