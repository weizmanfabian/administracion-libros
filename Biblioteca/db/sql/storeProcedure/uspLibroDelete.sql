
CREATE OR REPLACE PROCEDURE uspLibroDelete(
    IN l_libro_id INT
)
LANGUAGE plpgsql
AS $$
BEGIN
  --Validamos si existe el id
    IF NOT EXISTS (SELECT 1 FROM libros WHERE libro_id = l_libro_id) THEN
         RAISE EXCEPTION 'El libro no existe';
    END IF;

    -- eliminamos el registro
    DELETE FROM libros WHERE libro_id = l_libro_id;

EXCEPTION
  WHEN OTHERS THEN
    RAISE EXCEPTION '%', SQLERRM;
END;
$$;