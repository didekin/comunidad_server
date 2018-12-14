SET FOREIGN_KEY_CHECKS = 0;

# ==============================================
# ........... INCIDENCIAS ..........
# ==============================================

DROP TABLE IF EXISTS ambito_incidencia;
DROP TABLE IF EXISTS incidencia;

CREATE TABLE ambito_incidencia
(
  ambito_id SMALLINT UNSIGNED NOT NULL,
  ambito_ES VARCHAR(100)      NOT NULL, -- rótulo en español.
  PRIMARY KEY (ambito_id)
);

# TODO: en las TX incluir descripción que permita seguir historial de la incidencia.
CREATE TABLE incidencia
(
  incid_id    INTEGER UNSIGNED  NOT NULL AUTO_INCREMENT,
  cm_id       INTEGER UNSIGNED  NOT NULL,
  descripcion VARCHAR(300)      NOT NULL,
  ambito      SMALLINT UNSIGNED NOT NULL,
  importancia SMALLINT UNSIGNED NOT NULL, -- valores: 1, 2, 3 y 4.
  state       ENUM ('op', 'cl') NOT NULL,
  PRIMARY KEY (incid_id),
  INDEX id_parent_comu_miembro (cm_id),
  INDEX id_parent_ambito (ambito),
  FOREIGN KEY (cm_id) REFERENCES comunidad_miembro (cm_id)
    ON DELETE CASCADE,
  FOREIGN KEY (ambito) REFERENCES ambito_incidencia (ambito_id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT
);

SET FOREIGN_KEY_CHECKS = 1;