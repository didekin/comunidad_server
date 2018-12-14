SET FOREIGN_KEY_CHECKS = 0;

# ==============================================
# ........... COMUNIDADES Y USUARIOS ..........
# ==============================================

DROP TABLE IF EXISTS comunidad_autonoma;
DROP TABLE IF EXISTS comunidad_miembro;
DROP TABLE IF EXISTS entidad;
DROP TABLE IF EXISTS entidad_rel_entidad;
DROP TABLE IF EXISTS entidades_rel_usuario;
DROP TABLE IF EXISTS municipio;
DROP TABLE IF EXISTS proveedor_empleado;
DROP TABLE IF EXISTS provincia;
DROP TABLE IF EXISTS usuario;
DROP TABLE IF EXISTS usuario_appinstance;

CREATE TABLE comunidad_autonoma (
  ca_id  SMALLINT UNSIGNED NOT NULL,
  nombre VARCHAR(100)      NOT NULL,
  PRIMARY KEY (ca_id)
);

CREATE TABLE comunidad_miembro
(
  cm_id       INTEGER UNSIGNED  NOT NULL AUTO_INCREMENT,
  e_id        INTEGER UNSIGNED  NOT NULL,
  u_id        INTEGER UNSIGNED  NOT NULL,
  portal      VARCHAR(10)       NULL,
  escalera    VARCHAR(10)       NULL,
  planta      VARCHAR(10)       NULL,
  puerta      VARCHAR(10)       NULL,
  isApoderado BOOLEAN                    default false,
  state       ENUM ('op', 'cl') NOT NULL,
  PRIMARY KEY (cm_id),
  INDEX id_parent_com (e_id),
  INDEX id_parent_usu (u_id),
  FOREIGN KEY (e_id)
  REFERENCES entidad (e_id)
    ON DELETE CASCADE,
  FOREIGN KEY (u_id)
  REFERENCES usuario (u_id)
    ON DELETE CASCADE
);

CREATE TABLE entidad
(
  e_id          INTEGER UNSIGNED  NOT NULL AUTO_INCREMENT,
  id_fiscal     VARCHAR(10)       NULL,
  tipo_via      VARCHAR(25)       NOT NULL,
  nombre_via    VARCHAR(150)      NOT NULL,
  numero        SMALLINT UNSIGNED NOT NULL,
  sufijo_numero CHAR(10)          NULL,
  m_id          INTEGER UNSIGNED  NOT NULL,
  tipo_entidad  ENUM ('comunidad', 'prov_admon', 'prov_otros'),
  state         ENUM ('op', 'cl') NOT NULL,
  PRIMARY KEY (e_id),
  INDEX id_parent_municipio (m_id),
  FOREIGN KEY (m_id) REFERENCES municipio (m_id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT
);

CREATE TABLE entidad_rel_entidad
(
  ere_id INTEGER UNSIGNED  NOT NULL AUTO_INCREMENT,
  e1_id  INTEGER UNSIGNED  NOT NULL,
  e2_id  INTEGER UNSIGNED  NOT NULL,
  state  ENUM ('op', 'cl') NOT NULL,
  PRIMARY KEY (ere_id),
  INDEX id_parent_entidad1 (e1_id),
  INDEX id_parent_entidad2 (e2_id),
  FOREIGN KEY (e1_id)
  REFERENCES entidad (e_id)
    ON DELETE RESTRICT,
  FOREIGN KEY (e2_id)
  REFERENCES entidad (e_id)
);

CREATE TABLE entidades_rel_usuario
(
  esru_id     INTEGER UNSIGNED  NOT NULL AUTO_INCREMENT,
  e1_id       INTEGER UNSIGNED  NOT NULL,
  e2_id       INTEGER UNSIGNED  NOT NULL,
  u_id        INTEGER UNSIGNED  NOT NULL,
  isApoderado BOOLEAN                    default false,
  state       ENUM ('op', 'cl') NOT NULL,
  PRIMARY KEY (esru_id),
  INDEX id_parent_entidad1 (e1_id),
  INDEX id_parent_entidad2 (e2_id),
  INDEX id_parent_usuario (u_id),
  FOREIGN KEY (e1_id)
  REFERENCES entidad (e_id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,
  FOREIGN KEY (e2_id)
  REFERENCES entidad (e_id),
  FOREIGN KEY (u_id)
  REFERENCES usuario (u_id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT
);

CREATE TABLE municipio
(
  m_id   INTEGER UNSIGNED  NOT NULL AUTO_INCREMENT,
  pr_id  SMALLINT UNSIGNED NOT NULL, -- código de provincia del fichero de carga.
  m_cd   SMALLINT UNSIGNED NOT NULL, -- codigo municipio intra-provincial. No único.
  nombre VARCHAR(100)      NOT NULL,
  PRIMARY KEY (m_id),
  UNIQUE (pr_id, m_cd),
  INDEX id_parent_provincia (pr_id),
  FOREIGN KEY (pr_id) REFERENCES provincia (pr_id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT
);

CREATE TABLE proveedor_empleado
(
  pre_id INTEGER UNSIGNED  NOT NULL AUTO_INCREMENT,
  e_id   INTEGER UNSIGNED  NOT NULL,
  u_id   INTEGER UNSIGNED  NOT NULL,
  state  ENUM ('op', 'cl') NOT NULL,
  PRIMARY KEY (pre_id),
  INDEX id_parent_com (e_id),
  INDEX id_parent_usu (u_id),
  FOREIGN KEY (e_id)
  REFERENCES entidad (e_id)
    ON DELETE CASCADE,
  FOREIGN KEY (u_id)
  REFERENCES usuario (u_id)
    ON DELETE CASCADE
);

CREATE TABLE provincia
(
  pr_id  SMALLINT UNSIGNED NOT NULL, -- código INE de la provincia
  ca_id  SMALLINT UNSIGNED NOT NULL,
  nombre VARCHAR(100)      NOT NULL,
  PRIMARY KEY (pr_id),
  INDEX id_parent_comunidad (ca_id),
  FOREIGN KEY (ca_id) REFERENCES comunidad_autonoma (ca_id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT
);

CREATE TABLE usuario
(
  u_id       INTEGER UNSIGNED  NOT NULL AUTO_INCREMENT,
  alias      VARCHAR(30)       NOT NULL,
  password   VARCHAR(125)      NOT NULL,
  --   prefix_tf SMALLINT UNSIGNED NULL,
  --   num_tf INTEGER UNSIGNED NOT NULL,
  user_name  VARCHAR(60)       NOT NULL, -- email.
  token_auth CHAR(60)          NULL,
  state      ENUM ('op', 'cl') NOT NULL,
  PRIMARY KEY (u_id),
  UNIQUE (user_name)
);

CREATE TABLE usuario_appinstance
(
  u_id         INTEGER UNSIGNED  NOT NULL,
  ec_pub_key_x VARCHAR(100)      NOT NULL,
  ec_pub_key_y VARCHAR(100)      NOT NULL,
  gcm_token    VARCHAR(175)      NULL,
  state        ENUM ('op', 'cl') NOT NULL,
  UNIQUE (u_id, ec_pub_key_x, gcm_token),
  UNIQUE (u_id, ec_pub_key_y, gcm_token),
  INDEX id_parent_usuario (u_id),
  FOREIGN KEY (u_id)
  REFERENCES usuario (u_id)
    ON DELETE CASCADE
);

SET FOREIGN_KEY_CHECKS = 1;



