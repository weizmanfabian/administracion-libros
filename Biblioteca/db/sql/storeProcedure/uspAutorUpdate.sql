CREATE OR REPLACE PROCEDURE uspAutorUpdate(
    IN p_autor_id INT,
    IN p_nombre VARCHAR(255),
    IN p_apellido VARCHAR(255),
    IN p_nacionalidad VARCHAR(100) DEFAULT NULL
)
LANGUAGE plpgsql
AS $$
BEGIN
  --Validamos si existe el id
  IF NOT EXISTS (SELECT 1 FROM autor WHERE autor_id = p_autor_id) THEN
    RAISE EXCEPTION 'El autor con id % no existe', p_autor_id;
  END IF;

  -- Validamos parámetros obligatorios
  IF p_nombre IS NULL OR TRIM(p_nombre) = '' THEN
    RAISE EXCEPTION 'El parámetro nombre no puede ser nulo o vacío';
  END IF;

  IF p_apellido IS NULL OR TRIM(p_apellido) = '' THEN
    RAISE EXCEPTION 'El parámetro apellido no puede ser nulo o vacío';
  END IF;

  -- Actualizamos el registro
  UPDATE autor SET
    nombre = p_nombre,
    apellido = p_apellido,
    nacionalidad = CASE
                    WHEN p_nacionalidad IS NOT NULL AND TRIM(p_nacionalidad) != ''
                    THEN p_nacionalidad
                    ELSE nacionalidad
                  END
  WHERE autor_id = p_autor_id;

EXCEPTION
  WHEN OTHERS THEN
    RAISE EXCEPTION '%', SQLERRM;
END;
$$;