--liquibase formatted sql

--changeset pedronevado:4 dbms:mysql
ALTER TABLE comunidad
  drop index tipo_via,
  drop column fecha_alta,
  drop column fecha_mod,
  ADD COLUMN id_fiscal VARCHAR(9) NULL,
  ADD COLUMN state ENUM ('op', 'cl') NOT NULL;

--changeset pedronevado:5 dbms:mysql
CREATE TABLE comunidad_apoderado
(
  c_id         INTEGER UNSIGNED NOT NULL,
  u_id         INTEGER UNSIGNED NOT NULL,
  fecha_inicio TIMESTAMP        NOT NULL,
  fecha_fin    TIMESTAMP        NULL,
  PRIMARY KEY (c_id, u_id),
  INDEX id_parent_com (c_id),
  INDEX id_parent_usu (u_id),
  FOREIGN KEY (c_id)
  REFERENCES comunidad (c_id)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  FOREIGN KEY (u_id)
  REFERENCES usuario (u_id)
    ON UPDATE CASCADE
    ON DELETE CASCADE
);

--changeset pedronevado:6 dbms:mysql
ALTER TABLE usuario_comunidad
RENAME TO comunidad_miembro,
  drop column roles,
  drop column fecha_alta,
  drop column fecha_mod,
  ADD COLUMN state ENUM ('op', 'cl') NOT NULL;

--changeset pedronevado:5 dbms:mysql
ALTER TABLE usuario
  drop column fecha_alta,
  drop column fecha_mod,
  ADD COLUMN state ENUM ('op', 'cl') NOT NULL;

--changeset pedronevado:7 dbms:mysql
CREATE TABLE usuario_appinstance
(
  u_id      INTEGER UNSIGNED  NOT NULL,
  pub_key   VARCHAR(100)      NOT NULL,
  gcm_token VARCHAR(175)      NULL,
  state     ENUM ('op', 'cl') NOT NULL,
  UNIQUE (u_id, pub_key, gcm_token),
  INDEX id_parent_usuario (u_id),
  FOREIGN KEY (u_id)
  REFERENCES usuario (u_id)
    ON UPDATE CASCADE
    ON DELETE CASCADE
);

--changeset pedronevado:8 dbms:mysql
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

--changeset pedronevado:9 dbms:mysql
ALTER VIEW incid_importancia_user_view AS
  SELECT DISTINCT im.incid_id, im.c_id, im.u_id, u.user_name, u.alias
  FROM incidencia_importancia AS im
         INNER JOIN usuario_comunidad AS uc ON im.u_id = uc.u_id AND im.c_id = uc.c_id
         INNER JOIN usuario AS u ON im.u_id = u.u_id;

--changeset pedronevado:10 dbms:mysql
ALTER VIEW comunidades_municipio_view AS
  SELECT c.*, m.pr_id, m.m_cd, m.nombre AS m_nombre, pr.nombre AS pr_nombre, ca.ca_id
  FROM comunidad AS c
         INNER JOIN municipio AS m
         INNER JOIN provincia AS pr
         INNER JOIN comunidad_autonoma AS ca ON c.m_id = m.m_id
                                                  AND m.pr_id = pr.pr_id
                                                  AND pr.ca_id = ca.ca_id;

--changeset pedronevado:11 dbms:mysql
ALTER VIEW incid_importancia_resolucion_view AS
  SELECT DISTINCT im.incid_id,
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
         INNER JOIN incid_importancia_user_view AS iuv ON im.incid_id = iuv.incid_id AND im.u_id = iuv.u_id
         INNER JOIN incidencia_comunidad_view AS icv ON iuv.incid_id = icv.incid_id
         LEFT JOIN incidencia_resolucion AS re ON icv.incid_id = re.incid_id
  WHERE icv.fecha_cierre IS NULL;



