SET FOREIGN_KEY_CHECKS = 0;

DROP VIEW IF EXISTS administrador_vw;
DROP VIEW IF EXISTS administrador_empleado_vw;
DROP VIEW IF EXISTS admon_comunidad_vw;
DROP VIEW IF EXISTS comunidad_vw;
DROP VIEW IF EXISTS comu_administrador_vw;
DROP VIEW IF EXISTS comunidades_municipio_vw;
# DROP VIEW IF EXISTS usuarios_comunidades_view;

-- Updatable.
CREATE VIEW comunidad_vw AS
  SELECT *
  FROM entidad
  where tipo_entidad = 'comunidad' with check option;

CREATE VIEW administrador_vw AS
  SELECT *
  FROM entidad
  where tipo_entidad = 'prov_admon'
    and id_fiscal is not NULL with check option;

-- Relación entidad-miembro de tipo pertenencia a administrador (o empleado de/en administrador).
CREATE VIEW administrador_empleado_vw AS
  SELECT pre.*
  FROM administrador_vw AS adm
         INNER JOIN proveedor_empleado as pre USING (e_id)
  ORDER BY adm.e_id;

-- Updatable. It forces comunidad to be the first fk.
CREATE VIEW admon_comunidad_vw AS
  SELECT ere.ere_id,
         ere.e1_id    as comu_id,
         ere.e2_id    as adm_id,
         ere.state    as ere_state,
         co.id_fiscal as comu_id_fiscal,
         co.tipo_via,
         co.nombre_via,
         co.numero,
         co.sufijo_numero,
         co.m_id,
         co.state     as comu_state
  FROM administrador_vw as ad
         INNER JOIN entidad_rel_entidad as ere
         INNER JOIN comunidad_vw as co ON ad.e_id = ere.e2_id
                                            AND ere.e1_id = co.e_id
  ORDER BY ad.e_id, co.e_id;

-- Updatable. It forces comunidad to be the first fk. It selects/update only the comunidad's administrador.
CREATE VIEW comu_administrador_vw AS
  SELECT esru.esru_id,
         esru.state    as esru_state,
         esru.e1_id    as comu_id,
         esru.e2_id    as adm_id,
         esru.u_id     as adm_u_id,
         adm.id_fiscal as adm_idfiscal,
         adm.tipo_via,
         adm.nombre_via,
         adm.numero,
         adm.sufijo_numero,
         adm.m_id,
         adm.state     as adm_state
  FROM comunidad_vw as co
         INNER JOIN entidades_rel_usuario as esru
         INNER JOIN administrador_vw as adm ON co.e_id = esru.e1_id
                                                 AND esru.e2_id = adm.e_id
  WHERE esru.isApoderado = TRUE
  ORDER BY co.e_id, adm.e_id with check option;

CREATE VIEW comunidades_municipio_vw AS
  SELECT c.*, m.pr_id, m.m_cd, m.nombre AS m_nombre, pr.nombre AS pr_nombre, ca.ca_id
  FROM comunidad_vw AS c
         INNER JOIN municipio AS m
         INNER JOIN provincia AS pr
         INNER JOIN comunidad_autonoma AS ca ON c.m_id = m.m_id
                                                  AND m.pr_id = pr.pr_id
                                                  AND pr.ca_id = ca.ca_id;

SET FOREIGN_KEY_CHECKS = 1;


/*CREATE VIEW usuarios_comunidades_view AS
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
         AND m.pr_id = pr.pr_id;*/



