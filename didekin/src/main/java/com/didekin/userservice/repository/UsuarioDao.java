package com.didekin.userservice.repository;

import com.didekin.common.EntityException;
import com.didekinlib.gcm.model.common.GcmTokensHolder;
import com.didekinlib.model.comunidad.Comunidad;
import com.didekinlib.model.comunidad.Municipio;
import com.didekinlib.model.comunidad.Provincia;
import com.didekinlib.model.usuario.Usuario;
import com.didekinlib.model.usuariocomunidad.UsuarioComunidad;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static com.didekin.userservice.repository.UsuarioSql.DELETE_BY_NAME;
import static com.didekin.userservice.repository.UsuarioSql.DELETE_GCM_TOKEN;
import static com.didekin.userservice.repository.UsuarioSql.DELETE_USER_COMUNIDAD;
import static com.didekin.userservice.repository.UsuarioSql.IS_USER_IN_COMUNIDAD;
import static com.didekin.userservice.repository.UsuarioSql.USERCOMUS_BY_COMU;
import static com.didekin.userservice.repository.UsuarioSql.USERCOMU_BY_COMU;
import static com.didekin.userservice.repository.UsuarioSql.USERCOMU_BY_EMAIL;
import static com.didekin.userservice.repository.UsuarioSql.USUARIO_BY_EMAIL;
import static com.didekinlib.model.usuario.UsuarioExceptionMsg.USER_NAME_NOT_FOUND;
import static com.didekinlib.model.usuariocomunidad.UsuarioComunidadExceptionMsg.USERCOMU_WRONG_INIT;
import static com.didekinlib.model.usuariocomunidad.UsuarioComunidadExceptionMsg.USER_COMU_NOT_FOUND;

/**
 * User: pedro
 * Date: 31/03/15
 * Time: 09:37
 */
@Repository
public class UsuarioDao {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioDao.class.getCanonicalName());

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public UsuarioDao(JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    JdbcTemplate getJdbcTemplate()
    {
        return jdbcTemplate;
    }

    //    ============================================================
    //    ..........................  METHODS ........................
    //    ============================================================

    int deleteGcmToken(String originalGcmTk)
    {
        logger.info("deleteGcmToken(),jdbcUrl: " + ((org.apache.tomcat.jdbc.pool.DataSource) jdbcTemplate.getDataSource()).getUrl());
        return jdbcTemplate.update(DELETE_GCM_TOKEN.toString(), originalGcmTk);
    }

    boolean deleteUser(String userName) throws EntityException
    {
        logger.info("deleteUser()");

        int rowsDeleted = jdbcTemplate.update(DELETE_BY_NAME.toString(), userName);
        if (!(rowsDeleted == 1)) {
            throw new EntityException(USER_NAME_NOT_FOUND);
        }
        return Boolean.TRUE;
    }

    int deleteUserComunidad(UsuarioComunidad usuarioComunidad) throws EntityException
    {
        logger.info("deleteUserComunidad(),jdbcUrl: " + ((org.apache.tomcat.jdbc.pool.DataSource) jdbcTemplate.getDataSource()).getUrl());

        Comunidad comunidad = usuarioComunidad.getComunidad();

        int rowsDeleted = jdbcTemplate.update(DELETE_USER_COMUNIDAD.toString(),
                comunidad.getC_Id(),
                usuarioComunidad.getUsuario().getuId());

        if (!(rowsDeleted == 1)) {
            throw new EntityException(USER_COMU_NOT_FOUND);
        }
        return rowsDeleted;
    }

    List<String> getAllRolesFunctionalUser(String userName)
    {
        logger.info("getAllRolesFunctionalUser() ,jdbcUrl: " + ((org.apache.tomcat.jdbc.pool.DataSource) jdbcTemplate.getDataSource()).getUrl());

        List<String> roles = jdbcTemplate.queryForList(
                UsuarioSql.ROLES_ALL_FUNC.toString(), String.class, userName);

        List<String> functionalRoles = new ArrayList<>();

        Pattern pattern = Pattern.compile(",");

        for (String rolesByComunidad : roles) {
            String[] rolesDivided = pattern.split(rolesByComunidad);
            for (String roleDivided : rolesDivided) {
                if (!functionalRoles.contains(roleDivided)) {
                    functionalRoles.add(roleDivided);
                }
            }
        }
        return functionalRoles;
    }

    List<Comunidad> getComusByUser(String userName)
    {
        logger.info("getComusByUser() ,jdbcUrl: " + ((org.apache.tomcat.jdbc.pool.DataSource) jdbcTemplate.getDataSource()).getUrl());
        return jdbcTemplate.query(UsuarioSql.COMUS_BY_USER.toString(), new Object[]{userName},
                new ComunidadDao.ComunidadMapper());
    }

    List<String> getGcmTokensByComunidad(long comunidadId)
    {
        logger.debug("getGcmTokensByComunidad(), jdbcUrl: " + ((org.apache.tomcat.jdbc.pool.DataSource) jdbcTemplate.getDataSource()).getUrl());
        return jdbcTemplate.queryForList(UsuarioSql.GCM_TOKENS_BY_COMUNIDAD.toString(), String.class, comunidadId);
    }

    long getMaxPk()
    {
        Integer maxPk = jdbcTemplate.queryForObject(UsuarioSql.MAX_PK.toString(), Integer.class);
        return (maxPk == null) ? -1L : maxPk;
    }

    long getOldestUserComuId(long comunidadId)
    {
        logger.debug("getOldestUserComuId(), jdbcUrl: " + ((org.apache.tomcat.jdbc.pool.DataSource) jdbcTemplate.getDataSource()).getUrl());

        return jdbcTemplate.queryForObject(
                UsuarioSql.OLDEST_USER_COMU.toString(),
                new Long[]{comunidadId},
                (resultSet, rowNum) -> resultSet.getLong("u_id")
        );
    }

    Usuario getUserByUserName(String userName) throws UsernameNotFoundException
    {
        logger.info("getUserByUserName(), jdbcUrl: " + ((org.apache.tomcat.jdbc.pool.DataSource) jdbcTemplate.getDataSource()).getUrl());

        Usuario usuario;
        try {
            usuario = jdbcTemplate.queryForObject(
                    USUARIO_BY_EMAIL.toString(), new UsuarioMapper(), userName);
        } catch (EmptyResultDataAccessException e) {
            throw new EntityException(USER_NAME_NOT_FOUND);
        }
        return usuario;
    }

    Usuario getUsuarioById(long idUsuario) throws EntityException
    {
        logger.info("getUsuarioById(), jdbcUrl: " + ((org.apache.tomcat.jdbc.pool.DataSource) jdbcTemplate.getDataSource()).getUrl());

        Usuario usuario;
        try {
            usuario = jdbcTemplate.queryForObject(UsuarioSql.BY_ID.toString(),
                    new UsuarioMapper(), idUsuario);
        } catch (EmptyResultDataAccessException e) {
            logger.error(e.getMessage());
            throw new EntityException(USER_NAME_NOT_FOUND);
        }
        return usuario;
    }

    /**
     * @return a fully initialized UsuarioComuidad instance or null if the pair usuario-comunidad is not in DB.
     */
    UsuarioComunidad getUserComuFullByUserAndComu(String userName, long comunidadId) throws EntityException
    {
        logger.debug("getUserComuFullByUserAndComu(), jdbcUrl: " + ((org.apache.tomcat.jdbc.pool.DataSource) jdbcTemplate.getDataSource()).getUrl());
        try {
            return jdbcTemplate.queryForObject(USERCOMU_BY_COMU.toString(),
                    new UsuarioFullComunidadMapper(), userName, comunidadId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    UsuarioComunidad getUserComuRolesByUserName(String userName, long comunidadId)
    {
        logger.debug("getUserComuRolesByUserName()");
        try {
            return jdbcTemplate.queryForObject(
                    USERCOMU_BY_EMAIL.toString(),
                    new Object[]{userName, comunidadId},
                    (rs, rowNum) -> doUserComuOnlyRoles(
                            rs,
                            doUsuarioNoPswd(rs),
                            new Comunidad.ComunidadBuilder().c_id(rs.getLong("c_id")).build()
                    )
            );
        } catch (EmptyResultDataAccessException e) {
            throw new EntityException(USERCOMU_WRONG_INIT);
        }
    }

    Usuario getUsuarioWithGcmToken(long usuarioId)
    {
        logger.debug("getUsuarioWithGcmToken(), jdbcUrl: " + ((org.apache.tomcat.jdbc.pool.DataSource) jdbcTemplate.getDataSource()).getUrl());
        return jdbcTemplate.queryForObject(UsuarioSql.USER_WITH_GCMTOKEN.toString(), new UsuarioMapperForGcm(), usuarioId);
    }

    int modifyUserComu(UsuarioComunidad userComu)
    {
        logger.info("modifyUserComu()");

        return jdbcTemplate.update(UsuarioSql.MODIFY_USERCOMU.toString(),
                userComu.getPortal(),
                userComu.getEscalera(),
                userComu.getPlanta(),
                userComu.getPuerta(),
                userComu.getRoles(),
                userComu.getComunidad().getC_Id(),
                userComu.getUsuario().getuId());
    }

    /**
     * Returns the id of the inserted user.
     */
    @SuppressWarnings("Duplicates")
    long insertUsuario(final Usuario usuario, Connection conn) throws SQLException
    {
        logger.info("insertUsuario(), jdbcUrl: " + ((org.apache.tomcat.jdbc.pool.DataSource) jdbcTemplate.getDataSource()).getUrl());
        ResultSet rs;
        long usuarioId;

        try (PreparedStatement ps = conn.prepareStatement(UsuarioSql.INSERT.toString(), new String[]{UsuarioSql.PK.toString()})) {
            ps.setNull(1, JDBCType.INTEGER.getVendorTypeNumber());
            ps.setString(2, usuario.getAlias());
            ps.setString(3, usuario.getPassword());
            ps.setString(4, usuario.getUserName());
            ps.setString(5, usuario.getGcmToken());
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                usuarioId = rs.getLong(1);
            } else {
                throw new SQLException("getGeneratedKeys() produced no value");
            }
        }
        return usuarioId;
    }

    public boolean isUserInComunidad(String userName, long comunidadId)
    {
        logger.debug("isUserInComunidad()");
        return jdbcTemplate.queryForObject(
                IS_USER_IN_COMUNIDAD.toString(),
                new Object[]{userName, comunidadId},
                Integer.class) > 0;
    }

    /**
     * 'Public' to allows for a userController test.
     */
    int modifyUser(Usuario usuario)
    {
        logger.debug("modifyUser(), jdbcUrl: " + ((org.apache.tomcat.jdbc.pool.DataSource) jdbcTemplate.getDataSource()).getUrl());

        return jdbcTemplate.update(UsuarioSql.MODIFY_USER.toString(),
                usuario.getAlias(),
                usuario.getUserName(),
                usuario.getuId());
    }

    int modifyUserAlias(Usuario user)
    {
        logger.debug("modifyUserAlias()");

        return jdbcTemplate.update(UsuarioSql.MODIFY_USER_ALIAS.toString(),
                user.getAlias(),
                user.getuId());
    }

    int modifyUserGcmToken(Usuario usuario)
    {
        logger.debug("modifyUserGcmToken(), jdbcUrl: " + ((org.apache.tomcat.jdbc.pool.DataSource) jdbcTemplate.getDataSource()).getUrl());
        return jdbcTemplate.update(UsuarioSql.MODIFY_GCM_TOKEN_BY_USER.toString(),
                usuario.getGcmToken(), usuario.getuId());
    }

    int modifyUserGcmToken(GcmTokensHolder holder)
    {
        return jdbcTemplate.update(UsuarioSql.MODIFY_GCM_TOKEN_BY_TOKEN.toString(), holder.getNewGcmTk(), holder.getOriginalGcmTk());
    }

    List<UsuarioComunidad> seeUserComusByComu(long idComunidad)
    {
        logger.info("seeUserComusByComu(), jdbcUrl: " + ((org.apache.tomcat.jdbc.pool.DataSource) jdbcTemplate.getDataSource()).getUrl());
        List<UsuarioComunidad> usuariosComunidad = jdbcTemplate.query(USERCOMUS_BY_COMU.toString(), new
                Object[]{idComunidad}, new UsuarioComunidadMapper());
        logger.debug("seeUserComusByComu(); usuariosComunidad.size = " + usuariosComunidad.size());
        return usuariosComunidad;
    }

    int passwordChange(Usuario usuario)
    {
        logger.info("passwordChangeWithName(), jdbcUrl: " + ((org.apache.tomcat.jdbc.pool.DataSource) jdbcTemplate.getDataSource()).getUrl());

        return jdbcTemplate.update(UsuarioSql.NEW_PASSWORD.toString(),
                usuario.getPassword(),
                usuario.getuId());
    }

    List<UsuarioComunidad> seeUserComusByUser(String userName)
    {
        logger.info("seeUserComusByUser(), jdbcUrl: " + ((org.apache.tomcat.jdbc.pool.DataSource) jdbcTemplate.getDataSource()).getUrl());

        return jdbcTemplate.query(
                UsuarioSql.USERCOMUS_BY_USER.toString(),
                new Object[]{userName},
                new UsuarioFullComunidadMapper());
    }

    // .................. MAPPER CLASSES ......................

    private static final class UsuarioMapper implements RowMapper<Usuario> {

        @Override
        public Usuario mapRow(ResultSet rs, int i) throws SQLException
        {
            return new Usuario.UsuarioBuilder().copyUsuario(doUsuarioNoPswd(rs))
                    .password(rs.getString("password"))
                    .build();
        }
    }

    private static final class UsuarioFullComunidadMapper implements RowMapper<UsuarioComunidad> {

        @Override
        public UsuarioComunidad mapRow(ResultSet rs, int i) throws SQLException
        {
            Usuario usuario = doUsuarioNoPswd(rs);

            Comunidad comunidad = new Comunidad.ComunidadBuilder()
                    .c_id(rs.getLong("c_id"))
                    .tipoVia(rs.getString("tipo_via"))
                    .nombreVia(rs.getString("nombre_via"))
                    .numero(rs.getShort("numero"))
                    .sufijoNumero(rs.getString("sufijo_numero"))
                    .municipio(
                            new Municipio(
                                    rs.getShort("m_cd"),
                                    rs.getString("m_nombre"),
                                    new Provincia(
                                            rs.getShort("pr_id"),
                                            rs.getString("pr_nombre"))))
                    .build();

            return doUsuarioComunidadFull(rs, usuario, comunidad);
        }
    }

    private static final class UsuarioComunidadMapper implements RowMapper<UsuarioComunidad> {

        @Override
        public UsuarioComunidad mapRow(ResultSet rs, int i) throws SQLException
        {
            Usuario usuario = doUsuarioNoPswd(rs);
            Comunidad comunidad = new Comunidad.ComunidadBuilder()
                    .c_id(rs.getLong("c_id"))
                    .build();
            return doUsuarioComunidadFull(rs, usuario, comunidad);
        }
    }

    private static class UsuarioMapperForGcm implements RowMapper<Usuario> {
        @Override
        public Usuario mapRow(ResultSet rs, int i) throws SQLException
        {
            return new Usuario.UsuarioBuilder().copyUsuario(doUsuarioNoPswd(rs))
                    .gcmToken(rs.getString("gcm_token"))
                    .build();
        }
    }

    // .................. HELPER CLASSES ......................

    private static UsuarioComunidad doUsuarioComunidadFull(ResultSet rs, Usuario usuario, Comunidad comunidad) throws SQLException
    {
        return new UsuarioComunidad.UserComuBuilder(comunidad, usuario)
                .portal(rs.getString("portal"))
                .escalera(rs.getString("escalera"))
                .planta(rs.getString("planta"))
                .puerta(rs.getString("puerta"))
                .roles(rs.getString("roles"))
                .build();
    }

    private static UsuarioComunidad doUserComuOnlyRoles(ResultSet rs, Usuario usuario, Comunidad comunidad) throws SQLException
    {
        return new UsuarioComunidad.UserComuBuilder(comunidad, usuario)
                .roles(rs.getString("roles"))
                .build();
    }

    private static Usuario doUsuarioNoPswd(ResultSet rs) throws SQLException
    {
        return new Usuario.UsuarioBuilder()
                .uId(rs.getLong("u_id"))
                .userName(rs.getString("user_name"))
                .alias(rs.getString("alias"))
                .build();
    }
}