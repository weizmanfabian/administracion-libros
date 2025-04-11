
CREATE OR REPLACE PROCEDURE uspAutorInsert(
  IN p_nombre VARCHAR(255),
  IN p_apellido VARCHAR(255),
  IN p_nacionalidad VARCHAR(100),
  OUT new_id INTEGER
)
LANGUAGE plpgsql
AS $$
BEGIN
  -- Validamos parámetros obligatorios
  IF p_nombre IS NULL OR TRIM(p_nombre) = '' THEN
    RAISE EXCEPTION 'El nombre es requerido';
  END IF;

  IF p_apellido IS NULL OR TRIM(p_apellido) = '' THEN
    RAISE EXCEPTION 'El apellido es requerido';
  END IF;

  -- Insertamos el registro
  INSERT INTO autor (nombre, apellido, nacionalidad)
  VALUES (p_nombre, p_apellido, p_nacionalidad)
  RETURNING autor_id INTO new_id;

EXCEPTION
  WHEN OTHERS THEN
    RAISE EXCEPTION '%', SQLERRM;
END;
$$;