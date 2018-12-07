DROP VIEW IF EXISTS incid_importancia_user_view;
DROP VIEW IF EXISTS incidencia_comunidad_view;
DROP VIEW IF EXISTS incid_importancia_resolucion_view;
DROP VIEW IF EXISTS incidencia_avg_view;
DROP VIEW IF EXISTS incidencia_comment_view;
DROP VIEW IF EXISTS incidencia_user_alta_view;

CREATE VIEW incid_importancia_user_view AS
  SELECT DISTINCT
    im.incid_id,
    im.c_id,
    im.u_id,
    -- usuario who ranked importancia.
    u.user_name,
    u.alias,
    uc.roles
  FROM incidencia_importancia AS im
    INNER JOIN usuario_comunidad AS uc
      ON im.u_id = uc.u_id AND im.c_id = uc.c_id
    INNER JOIN usuario AS u
      ON im.u_id = u.u_id;

CREATE VIEW incidencia_comunidad_view AS
  SELECT DISTINCT
    ic.incid_id,
    ic.c_id,
      -- user who initiates the incidencia.
    ic.user_name,
    ic.descripcion,
    ic.ambito,
    ic.fecha_alta,
    ic.fecha_cierre,
    c.tipo_via,
    c.nombre_via,
    c.numero,
    c.sufijo_numero
  FROM incidencia AS ic
    INNER JOIN comunidad AS c
    USING (c_id);

# Open incidencias only.
CREATE VIEW incid_importancia_resolucion_view AS
  SELECT DISTINCT
    im.incid_id,
    im.u_id,
    -- usuario who ranked importancia.
    iuv.user_name,
    iuv.alias,
    iuv.roles,
    im.importancia,
    im.fecha_alta,
    icv.descripcion,
    icv.ambito,
    icv.fecha_alta    AS fecha_alta_incidencia,
    icv.user_name     AS incid_user_initiator,
    icv.c_id,
    icv.tipo_via      AS comunidad_tipo_via,
    icv.nombre_via    AS comunidad_nombre_via,
    icv.numero        AS comunidad_numero,
    icv.sufijo_numero AS comunidad_sufijo,
    re.fecha_alta     AS fecha_alta_resolucion
  FROM incidencia_importancia AS im
    INNER JOIN incid_importancia_user_view AS iuv
      ON im.incid_id = iuv.incid_id AND im.u_id = iuv.u_id
    INNER JOIN incidencia_comunidad_view AS icv
      ON iuv.incid_id = icv.incid_id
    LEFT JOIN incidencia_resolucion AS re
      ON icv.incid_id = re.incid_id
  WHERE icv.fecha_cierre IS NULL;

/* Calcula la importancia media de una incidencia.*/
CREATE VIEW incidencia_avg_view AS
  SELECT
    incid_id,
    AVG(importancia) AS importancia_avg
  FROM incidencia_importancia
  GROUP BY incid_id;

/* Presentación de comentarios de cada incidencia.*/
CREATE VIEW incidencia_comment_view AS
  SELECT
    ic.*,
    u.user_name,
    u.alias
  FROM incidencia_comment AS ic
    INNER JOIN usuario AS u
      ON ic.u_id = u.u_id;

CREATE VIEW incidencia_user_alta_view AS
  SELECT
    i.incid_id,
    i.c_id,
    i.descripcion,
    i.ambito,
    i.fecha_alta,
    i.fecha_cierre,
    i.user_name,
    u.alias,
    u.u_id
  FROM incidencia AS i
    LEFT JOIN usuario AS u
      ON i.user_name = u.user_name;
