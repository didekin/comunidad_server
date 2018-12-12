--liquibase formatted sql

--changeset pedronevado:130 dbms:mysql
CREATE TABLE entidad
(
  e_id          INTEGER UNSIGNED  NOT NULL AUTO_INCREMENT,
  id_fiscal     VARCHAR(10)       NULL,
  tipo_via      VARCHAR(25)       NOT NULL,
  nombre_via    VARCHAR(150)      NOT NULL,
  numero        SMALLINT UNSIGNED NOT NULL,
  sufijo_numero CHAR(10)          NULL,
  m_id          INTEGER UNSIGNED  NOT NULL,
  PRIMARY KEY (e_id),
  INDEX id_parent_municipio (m_id),
  FOREIGN KEY (m_id) REFERENCES municipio (m_id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT
);

--changeset pedronevado:131 dbms:mysql
CREATE TABLE proveedor
(
  p_id INTEGER UNSIGNED NOT NULL,
  PRIMARY KEY (p_id),
  FOREIGN KEY (p_id) REFERENCES entidad (e_id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT
);