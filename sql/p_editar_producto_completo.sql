-- Ejecutar en la base de datos "puntoventarosi" (PostgreSQL).
-- Procedure de edicion COMPLETA de un producto: actualiza todos los campos
-- editables, incluyendo cantidad, caducidad y proveedor (a diferencia de
-- p_editar_producto, que solo toca nombre/marca/precio/imagen).
--
-- Semantica de "fijar" (set): cantidad y caducidad quedan con el valor exacto
-- que se manda, NO se suman (para sumar stock existe p_abastecer).
--
-- Respeta las constraints de la tabla:
--   check_fecha    -> p_caducidad debe ser >= CURRENT_DATE
--   check_cantidad -> p_cantidad debe ser >= 0
--   fk_proveedor_producto -> p_idproveedor debe existir en proveedor

CREATE OR REPLACE PROCEDURE p_editar_producto_completo(
    p_idproducto  INT,
    p_nombre      TEXT,
    p_marca       TEXT,
    p_precio      DECIMAL(10,2),
    p_imagen_url  TEXT,
    p_cantidad    INT,
    p_caducidad   DATE,
    p_idproveedor INT
)
LANGUAGE plpgsql
AS $$
BEGIN
    UPDATE producto
       SET nombreproducto = p_nombre,
           marca          = p_marca,
           precio         = p_precio,
           imagen_url     = p_imagen_url,
           cantidad       = p_cantidad,
           caducidad      = p_caducidad,
           idproveedor    = p_idproveedor
     WHERE idproducto = p_idproducto;
END;
$$;
