# ==============================================
# ............ INCIDENCIAS .....................
# ==============================================

# Borrar incidencias y comentarios tras dos años cerrados.

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS incidencia_comment;
DROP TABLE IF EXISTS incidencia_res_avance;
DROP TABLE IF EXISTS incidencia_resolucion;
DROP TABLE IF EXISTS incidencia_importancia;
DROP TABLE IF EXISTS incidencia;
DROP TABLE IF EXISTS ambito_incidencia;

CREATE TABLE ambito_incidencia
(
  ambito_id SMALLINT UNSIGNED NOT NULL,
  ambito_ES VARCHAR(100)      NOT NULL, -- rótulo en español.
  PRIMARY KEY (ambito_id)
);

/* incidencia: 1 -----> 1 comunidad */
CREATE TABLE incidencia
(
  incid_id     INTEGER UNSIGNED  NOT NULL AUTO_INCREMENT,
  c_id         INTEGER UNSIGNED  NOT NULL,
  user_name    VARCHAR(60)       NOT NULL, -- email del usuario que da el alta.
  descripcion  VARCHAR(300)      NOT NULL,
  ambito       SMALLINT UNSIGNED NOT NULL,
  fecha_alta   TIMESTAMP(2)      NOT NULL DEFAULT CURRENT_TIMESTAMP(2),
  fecha_cierre TIMESTAMP(2)      NULL,
  PRIMARY KEY (incid_id),
  INDEX (incid_id, c_id),
  INDEX id_parent_comunidad (c_id),
  INDEX id_parent_ambito (ambito),
  FOREIGN KEY (c_id) REFERENCES comunidad (c_id)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  FOREIGN KEY (ambito) REFERENCES ambito_incidencia (ambito_id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT
);

/* incidencia: 1 -----> N indidencia_comment */
CREATE TABLE incidencia_comment
(
  comment_id  INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  incid_id    INTEGER UNSIGNED NOT NULL,
  c_id        INTEGER UNSIGNED NOT NULL,
  u_id        INTEGER UNSIGNED NOT NULL,
  descripcion VARCHAR(250)     NOT NULL,
  fecha_alta  TIMESTAMP(2)     NOT NULL DEFAULT CURRENT_TIMESTAMP(2),
  PRIMARY KEY (comment_id),
  INDEX id_parent_incidencia (incid_id),
  INDEX id_parent_usercomu(c_id, u_id),
  FOREIGN KEY (incid_id)
  REFERENCES incidencia (incid_id)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  FOREIGN KEY (c_id, u_id)
  REFERENCES usuario_comunidad (c_id, u_id)
    ON UPDATE CASCADE
    ON DELETE CASCADE
);

/* incidencia: 1 -----> N indidencia_importancia. Única importancia por usuario-incidencia (incidencia es 1 -> 1 por comunidad) */
CREATE TABLE incidencia_importancia
(
  incid_id    INTEGER UNSIGNED  NOT NULL,
  c_id        INTEGER UNSIGNED  NOT NULL,
  u_id        INTEGER UNSIGNED  NOT NULL,
  importancia SMALLINT UNSIGNED NOT NULL, -- valores: 1, 2, 3 y 4.
  fecha_alta  TIMESTAMP(2)      NOT NULL DEFAULT CURRENT_TIMESTAMP(2),
  PRIMARY KEY (incid_id, u_id),
  INDEX id_parent_incidencia_comunidad (incid_id),
  INDEX id_parent_usercomu(c_id, u_id),
  FOREIGN KEY (incid_id)
  REFERENCES incidencia (incid_id)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  FOREIGN KEY (c_id, u_id)
  REFERENCES usuario_comunidad (c_id, u_id)
    ON UPDATE CASCADE
    ON DELETE CASCADE
);

/* incidencia: 1 -----> 1 incidencia_resolucion */
CREATE TABLE incidencia_resolucion
(
  incid_id       INTEGER UNSIGNED NOT NULL,
  user_name      VARCHAR(60)      NOT NULL, -- email del usuario que da el alta.
  plan           VARCHAR(300)     NOT NULL,
  coste_estimado INTEGER SIGNED   NULL,
  fecha_prevista TIMESTAMP        NULL,
  coste          INTEGER SIGNED   NULL,
  moraleja       VARCHAR(250)     NULL,
  fecha_alta     TIMESTAMP(2)     NOT NULL DEFAULT CURRENT_TIMESTAMP(2),
  PRIMARY KEY (incid_id), -- una incidencia sólo puede tener una resolución.
  INDEX id_parent_incidencia_comunidad (incid_id),
  FOREIGN KEY (incid_id)
  REFERENCES incidencia (incid_id)
    ON UPDATE CASCADE
    ON DELETE CASCADE
);

/* resolucion: 1 -----> N resolucion_avance. */
CREATE TABLE incidencia_res_avance
(
  avance_id   INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  incid_id    INTEGER UNSIGNED NOT NULL,
  user_name   VARCHAR(60)      NOT NULL, -- email del usuario que da el alta.
  descripcion VARCHAR(250)     NOT NULL,
  fecha_alta  TIMESTAMP(2)     NOT NULL DEFAULT CURRENT_TIMESTAMP(2),
  PRIMARY KEY (avance_id),
  INDEX id_parent_incidencia (incid_id),
  FOREIGN KEY (incid_id)
  REFERENCES incidencia (incid_id)
    ON UPDATE CASCADE
    ON DELETE CASCADE
);

SET FOREIGN_KEY_CHECKS = 1;

# ....................... VIEWS .............................

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
    ic.user_name,
    -- user who initiates the incidencia.
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

