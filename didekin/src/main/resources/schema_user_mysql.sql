SET FOREIGN_KEY_CHECKS = 0;

# ==============================================
# ........... COMUNIDADES Y USUARIOS ..........
# ==============================================

DROP TABLE IF EXISTS comunidad;
DROP TABLE IF EXISTS usuario_comunidad;
DROP TABLE IF EXISTS oauth_client_token;
DROP TABLE IF EXISTS oauth_access_token;
DROP TABLE IF EXISTS oauth_refresh_token;
DROP TABLE IF EXISTS usuario;
DROP TABLE IF EXISTS municipio;
DROP TABLE IF EXISTS provincia;
DROP TABLE IF EXISTS comunidad_autonoma;

CREATE TABLE comunidad
(
  c_id          INTEGER UNSIGNED  NOT NULL AUTO_INCREMENT,
  tipo_via      VARCHAR(25)       NOT NULL,
  nombre_via    VARCHAR(150)      NOT NULL,
  numero        SMALLINT UNSIGNED NOT NULL,
  sufijo_numero CHAR(10)          NULL,
  m_id          INTEGER UNSIGNED  NOT NULL,
  fecha_alta    TIMESTAMP         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_mod     TIMESTAMP         NULL
  ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (c_id),
  UNIQUE (tipo_via, nombre_via, numero, sufijo_numero, m_id),
  INDEX id_parent_municipio (m_id),
  FOREIGN KEY (m_id) REFERENCES municipio (m_id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT
);

CREATE TABLE comunidad_autonoma (
  ca_id  SMALLINT UNSIGNED NOT NULL,
  nombre VARCHAR(100)      NOT NULL,
  PRIMARY KEY (ca_id)
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

CREATE TABLE oauth_access_token (
  token_id          VARCHAR(100),
  token             BLOB,
  authentication_id VARCHAR(100) PRIMARY KEY,
  user_name         VARCHAR(100) NOT NULL,
  client_id         VARCHAR(100),
  authentication    BLOB,
  refresh_token     VARCHAR(100) NOT NULL,
  fecha_alta        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE (user_name),
  INDEX refresh_token_index (refresh_token),
  FOREIGN KEY (user_name) REFERENCES usuario (user_name)
    ON UPDATE CASCADE
    ON DELETE CASCADE
);

CREATE TABLE oauth_refresh_token (
  token_id       VARCHAR(100) NOT NULL,
  token          BLOB,
  authentication BLOB,
  PRIMARY KEY (token_id),
  FOREIGN KEY (token_id) REFERENCES oauth_access_token (refresh_token)
    ON UPDATE CASCADE
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
  u_id       INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  alias      VARCHAR(30)      NOT NULL,
  password   VARCHAR(125)     NOT NULL,
  --   prefix_tf SMALLINT UNSIGNED NULL,
  --   num_tf INTEGER UNSIGNED NOT NULL,
  user_name  VARCHAR(60)      NOT NULL, -- email.
  gcm_token  VARCHAR(175)     NULL, -- google messages id token
  fecha_alta TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_mod  TIMESTAMP        NULL
  ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (u_id),
  UNIQUE (user_name),
  UNIQUE (gcm_token)
);

CREATE TABLE usuario_comunidad
(
  c_id       INTEGER UNSIGNED                 NOT NULL, -- id comunidad de pertenencia.
  u_id       INTEGER UNSIGNED                 NOT NULL, -- usuario de la dirección.
  portal     CHAR(10)                         NULL,
  escalera   CHAR(10)                         NULL,
  planta     CHAR(10)                         NULL,
  puerta     CHAR(10)                         NULL,
  roles      SET ('adm', 'pre', 'pro', 'inq') NOT NULL,
  fecha_alta TIMESTAMP                        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_mod  TIMESTAMP                        NULL
  ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (c_id, u_id),
  /*UNIQUE (c_id, portal, escalera, planta, puerta),*/
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

SET FOREIGN_KEY_CHECKS = 1;

-- ...........VIEWS..............

DROP VIEW IF EXISTS comunidades_municipio_view;
DROP VIEW IF EXISTS usuarios_comunidades_view;

CREATE VIEW comunidades_municipio_view AS
  SELECT
    c.*,
    m.pr_id,
    m.m_cd,
    m.nombre  AS m_nombre,
    pr.nombre AS pr_nombre,
    ca.ca_id
  FROM comunidad AS c
    INNER JOIN municipio AS m
    INNER JOIN provincia AS pr
    INNER JOIN comunidad_autonoma AS ca
      ON c.m_id = m.m_id
         AND m.pr_id = pr.pr_id
         AND pr.ca_id = ca.ca_id;

CREATE VIEW usuarios_comunidades_view AS
  SELECT
    u.user_name,
    u.u_id,
    alias,
    c.c_id,
    c.tipo_via,
    c.nombre_via,
    c.numero,
    c.sufijo_numero,
    c.fecha_alta,
    cu.portal,
    cu.escalera,
    cu.planta,
    cu.puerta,
    cu.roles,
    m.pr_id,
    m.m_cd,
    m.nombre  AS m_nombre,
    pr.nombre AS pr_nombre
  FROM usuario AS u
    INNER JOIN usuario_comunidad AS cu
    INNER JOIN comunidad AS c
    INNER JOIN municipio AS m
    INNER JOIN provincia AS pr
      ON u.u_id = cu.u_id
         AND cu.c_id = c.c_id
         AND c.m_id = m.m_id
         AND m.pr_id = pr.pr_id;



