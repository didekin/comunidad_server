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

/* incidservice: 1 -----> 1 comunidad */
CREATE TABLE incidencia
(
  incid_id     INTEGER UNSIGNED  NOT NULL AUTO_INCREMENT,
  c_id         INTEGER UNSIGNED  NOT NULL,
  user_name    VARCHAR(60)       NOT NULL, -- email del usuario que da el alta.
  descripcion  VARCHAR(300)      NOT NULL,
  ambito       SMALLINT UNSIGNED NOT NULL,
  fecha_alta   TIMESTAMP(2)         NOT NULL DEFAULT CURRENT_TIMESTAMP(2),
  fecha_cierre TIMESTAMP(2)        NULL,
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

/* incidservice: 1 -----> N indidencia_comment */
CREATE TABLE incidencia_comment
(
  comment_id  INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  incid_id    INTEGER UNSIGNED NOT NULL,
  c_id        INTEGER UNSIGNED NOT NULL,
  u_id        INTEGER UNSIGNED NOT NULL,
  descripcion VARCHAR(250)     NOT NULL,
  fecha_alta  TIMESTAMP(2)    NOT NULL DEFAULT CURRENT_TIMESTAMP(2),
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

/* incidservice: 1 -----> N indidencia_importancia. Única importancia por usuarioComunidad-incidservice. */
CREATE TABLE incidencia_importancia
(
  incid_id    INTEGER UNSIGNED  NOT NULL,
  c_id        INTEGER UNSIGNED  NOT NULL,
  u_id        INTEGER UNSIGNED  NOT NULL,
  importancia SMALLINT UNSIGNED NOT NULL, -- valores: 1, 2, 3 y 4.
  fecha_alta  TIMESTAMP(2) NOT NULL DEFAULT CURRENT_TIMESTAMP(2),
  PRIMARY KEY (incid_id, c_id, u_id),
  INDEX incidencia_usuario_index (incid_id, u_id),
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

/* incidservice: 1 -----> 1 incidencia_resolucion */
CREATE TABLE incidencia_resolucion
(
  incid_id       INTEGER UNSIGNED NOT NULL,
  user_name      VARCHAR(60)      NOT NULL, -- email del usuario que da el alta.
  plan           VARCHAR(300)     NOT NULL,
  coste_estimado INTEGER SIGNED   NULL,
  fecha_prevista TIMESTAMP        NULL,
  coste          INTEGER SIGNED   NULL,
  moraleja       VARCHAR(250)     NULL,
  fecha_alta     TIMESTAMP(2) NOT NULL DEFAULT CURRENT_TIMESTAMP(2),
  PRIMARY KEY (incid_id), -- una incidservice sólo puede tener una resolución.
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
DROP VIEW IF EXISTS incidencia_avg_view;
DROP VIEW IF EXISTS incidencia_comment_view;
DROP VIEW IF EXISTS incidencia_comunidad_view;
DROP VIEW IF EXISTS incidencia_user_alta_view;

CREATE VIEW incid_importancia_user_view AS
  SELECT DISTINCT
    ii.u_id,
    ii.incid_id,
    ii.c_id,
    ii.importancia,
    ii.fecha_alta,
    u.alias,
    u.user_name
  FROM incidencia_importancia AS ii
    INNER JOIN usuario AS u
      ON ii.u_id = u.u_id;

/* Calcula la importancia media de una incidservice.*/
CREATE VIEW incidencia_avg_view AS
  SELECT
    incid_id,
    AVG(importancia) AS importancia_avg
  FROM incidencia_importancia
  GROUP BY incid_id;

/* Presentación de comentarios de cada incidservice.*/
CREATE VIEW incidencia_comment_view AS
  SELECT
    ic.*,
    u.user_name,
    u.alias
  FROM incidencia_comment AS ic
    INNER JOIN usuario AS u
      ON ic.u_id = u.u_id;

CREATE VIEW incidencia_comunidad_view AS
  SELECT DISTINCT
    ic.incid_id,
    ic.c_id,
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
      ON ic.c_id = c.c_id;

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

