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



