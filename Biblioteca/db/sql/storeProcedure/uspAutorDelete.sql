
CREATE OR REPLACE PROCEDURE uspAutorDelete(
    IN p_autor_id INT
)
LANGUAGE plpgsql
AS $$
BEGIN
  --Validamos si existe el id
  IF NOT EXISTS (SELECT 1 FROM autor WHERE autor_id = p_autor_id) THEN
    RAISE EXCEPTION 'El autor con id % no existe', p_autor_id;
  END IF;

    -- eliminamos el registro
    DELETE FROM autor WHERE autor_id = p_autor_id;

EXCEPTION
  WHEN OTHERS THEN
    RAISE EXCEPTION '%', SQLERRM;
END;
$$;