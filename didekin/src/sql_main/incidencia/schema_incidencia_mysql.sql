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
  INDEX user_name_author (user_name),
  INDEX id_parent_incidencia (incid_id),
  FOREIGN KEY (incid_id)
  REFERENCES incidencia (incid_id)
    ON UPDATE CASCADE
    ON DELETE CASCADE
);

SET FOREIGN_KEY_CHECKS = 1;
