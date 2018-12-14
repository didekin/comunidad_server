SET FOREIGN_KEY_CHECKS = 0;

DROP VIEW IF EXISTS comu_incidencia_vw;

CREATE VIEW comu_incidencia_vw AS
  SELECT DISTINCT com.e_id as comu_id, ic.incid_id, ic.descripcion, ic.ambito
  FROM comunidad_miembro AS com
         INNER JOIN incidencia AS ic USING (cm_id);

SET FOREIGN_KEY_CHECKS = 1;