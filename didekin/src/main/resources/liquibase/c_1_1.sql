--liquibase formatted sql

--changeset pedronevado:110 dbms:mysql
ALTER TABLE comunidad
  drop index tipo_via,
  drop column fecha_alta,
  drop column fecha_mod,
  ADD COLUMN id_fiscal VARCHAR(9) NULL,
  ADD COLUMN state ENUM ('op', 'cl') NOT NULL;

--changeset pedronevado:117 dbms:mysql
SET FOREIGN_KEY_CHECKS = 0;
ALTER TABLE comunidad
  drop column id_fiscal,
  drop column tipo_via,
  drop column nombre_via,
  drop column numero,
  drop column sufijo_numero,
  drop column m_id,
  modify column c_id INTEGER UNSIGNED NOT NULL,
  ADD FOREIGN KEY (c_id) REFERENCES entidad (e_id)
  ON UPDATE CASCADE
  ON DELETE CASCADE;
SET FOREIGN_KEY_CHECKS = 1;

--changeset pedronevado:111 dbms:mysql runOnChange:true
CREATE TABLE comunidad_apoderado
(
  c_id         INTEGER UNSIGNED  NOT NULL,
  u_id         INTEGER UNSIGNED  NOT NULL,
  fecha_inicio TIMESTAMP         NOT NULL,
  fecha_fin    TIMESTAMP         NULL,
  state        ENUM ('op', 'cl') NOT NULL,
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

--changeset pedronevado:118 dbms:mysql runOnChange:true
CREATE VIEW comunidad_miembro_apoderado AS
  SELECT ca.*
  FROM comunidad_apoderado AS ca
         INNER JOIN comunidad_miembro AS cm ON ca.c_id = cm.c_id
                                                 AND ca.u_id = cm.u_id
  WHERE cm.state = 'op';

--changeset pedronevado:112 dbms:mysql
ALTER TABLE usuario_comunidad
RENAME TO comunidad_miembro,
  drop column roles,
  drop column fecha_alta,
  drop column fecha_mod,
  ADD COLUMN state ENUM ('op', 'cl') NOT NULL;

--changeset pedronevado:113 dbms:mysql
ALTER TABLE usuario
  drop column fecha_alta,
  drop column fecha_mod,
  ADD COLUMN state ENUM ('op', 'cl') NOT NULL;

--changeset pedronevado:114 dbms:mysql
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

--changeset pedronevado:115 dbms:mysql
ALTER TABLE incidencia_comment
  DROP FOREIGN KEY incidencia_comment_ibfk_2,
  ADD FOREIGN KEY (c_id, u_id) REFERENCES comunidad_miembro (c_id, u_id)
  ON UPDATE CASCADE
  ON DELETE CASCADE;

--changeset pedronevado:116 dbms:mysql
ALTER TABLE incidencia_importancia
  DROP FOREIGN KEY incidencia_importancia_ibfk_2,
  ADD FOREIGN KEY (c_id, u_id) REFERENCES comunidad_miembro (c_id, u_id)
  ON UPDATE CASCADE
  ON DELETE CASCADE;




