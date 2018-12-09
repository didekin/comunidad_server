--liquibase formatted sql

--changeset pedronevado:4 dbms:mysql
ALTER TABLE comunidad
  drop index tipo_via,
  drop column fecha_alta,
  drop column fecha_mod;

--changeset pedronevado:5 dbms:mysql
ALTER TABLE usuario
  drop column fecha_alta,
  drop column fecha_mod;

--changeset pedronevado:6 dbms:mysql
ALTER TABLE usuario_comunidad
  drop column roles,
  drop column fecha_alta,
  drop column fecha_mod;

--changeset pedronevado:7 dbms:mysql
ALTER VIEW usuarios_comunidades_view AS
  SELECT u.user_name,
         u.u_id,
         alias,
         c.c_id,
         c.tipo_via,
         c.nombre_via,
         c.numero,
         c.sufijo_numero,
         cu.portal,
         cu.escalera,
         cu.planta,
         cu.puerta,
         m.pr_id,
         m.m_cd,
         m.nombre  AS m_nombre,
         pr.nombre AS pr_nombre
  FROM usuario AS u
         INNER JOIN usuario_comunidad AS cu
         INNER JOIN comunidad AS c
         INNER JOIN municipio AS m
         INNER JOIN provincia AS pr ON u.u_id = cu.u_id
                                         AND cu.c_id = c.c_id
                                         AND c.m_id = m.m_id
                                         AND m.pr_id = pr.pr_id;

--changeset pedronevado:8 dbms:mysql
ALTER VIEW incid_importancia_user_view AS
  SELECT DISTINCT im.incid_id, im.c_id, im.u_id, u.user_name, u.alias
  FROM incidencia_importancia AS im
         INNER JOIN usuario_comunidad AS uc ON im.u_id = uc.u_id AND im.c_id = uc.c_id
         INNER JOIN usuario AS u ON im.u_id = u.u_id;

--changeset pedronevado:9 dbms:mysql
ALTER VIEW comunidades_municipio_view AS
  SELECT c.*, m.pr_id, m.m_cd, m.nombre AS m_nombre, pr.nombre AS pr_nombre, ca.ca_id
  FROM comunidad AS c
         INNER JOIN municipio AS m
         INNER JOIN provincia AS pr
         INNER JOIN comunidad_autonoma AS ca ON c.m_id = m.m_id
                                                  AND m.pr_id = pr.pr_id
                                                  AND pr.ca_id = ca.ca_id;

--changeset pedronevado:10 dbms:mysql
ALTER VIEW incid_importancia_resolucion_view AS
  SELECT DISTINCT
                  im.incid_id,
                  im.u_id,
      -- usuario who ranked importancia.
                  iuv.user_name,
                  iuv.alias,
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