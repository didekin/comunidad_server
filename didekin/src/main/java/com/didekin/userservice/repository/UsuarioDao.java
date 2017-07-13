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

import static com.didekinlib.model.usuario.UsuarioExceptionMsg.USER_NAME_NOT_FOUND;
import static com.didekinlib.model.usuariocomunidad.UsuarioComunidadExceptionMsg.ROLES_NOT_FOUND;
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
        return jdbcTemplate.update(UsuarioSql.DELETE_GCM_TOKEN.toString(), originalGcmTk);
    }

    boolean deleteUser(String userName) throws EntityException
    {
        logger.info("deleteUser()");

        int rowsDeleted = jdbcTemplate.update(UsuarioSql.DELETE_BY_NAME.toString(), userName);
        if (!(rowsDeleted == 1)) {
            logger.error(USER_NAME_NOT_FOUND.getHttpMessage());
            throw new EntityException(USER_NAME_NOT_FOUND);
        }
        return Boolean.TRUE;
    }

    int deleteUserComunidad(UsuarioComunidad usuarioComunidad) throws EntityException
    {
        logger.info("deleteUserComunidad(),jdbcUrl: " + ((org.apache.tomcat.jdbc.pool.DataSource) jdbcTemplate.getDataSource()).getUrl());

        Comunidad comunidad = usuarioComunidad.getComunidad();

        int rowsDeleted = jdbcTemplate.update(UsuarioSql.DELETE_USER_COMUNIDAD.toString(),
                comunidad.getC_Id(),
                usuarioComunidad.getUsuario().getuId());

        if (!(rowsDeleted == 1)) {
            logger.error(USER_COMU_NOT_FOUND.getHttpMessage());
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

    String[] getFuncionRolesArrayByUserComu(String userName, long comunidadId) throws EntityException
    {
        logger.debug("getFuncionRolesArrayByUserComu(), jdbcUrl: " + ((org.apache.tomcat.jdbc.pool.DataSource) jdbcTemplate.getDataSource()).getUrl());

        try {
            String roles = jdbcTemplate.queryForObject(UsuarioSql.ROL_BY_COMUNIDAD.toString(), String.class, userName, comunidadId);
            return Pattern.compile(",").split(roles);
        } catch (EmptyResultDataAccessException e) {
            logger.error(e.getMessage());
            throw new EntityException(ROLES_NOT_FOUND);
        }
    }

    String getFuncionRolesStringByUserComu(String userName, long comunidadId) throws EntityException
    {
        logger.debug("getFuncionRolesStringByUserComu(), jdbcUrl: " + ((org.apache.tomcat.jdbc.pool.DataSource) jdbcTemplate.getDataSource()).getUrl());

        try {
            return jdbcTemplate.queryForObject(UsuarioSql.ROL_BY_COMUNIDAD.toString(), String.class, userName, comunidadId);
        } catch (EmptyResultDataAccessException e) {
            logger.error(e.getMessage());
            throw new EntityException(ROLES_NOT_FOUND);
        }
    }

    List<String> getGcmTokensByComunidad(long comunidadId)
    {
        logger.debug("getGcmTokensByComunidad(), jdbcUrl: " + ((org.apache.tomcat.jdbc.pool.DataSource) jdbcTemplate.getDataSource()).getUrl());
        return jdbcTemplate.queryForList(UsuarioSql.GCM_TOKENS_BY_COMUNIDAD.toString(), String.class, comunidadId);
    }

    UsuarioComunidad getUserComuByUserAndComu(String userName, long comunidadId) throws EntityException
    {
        logger.debug("getUserComuByUserAndComu(), jdbcUrl: " + ((org.apache.tomcat.jdbc.pool.DataSource) jdbcTemplate.getDataSource()).getUrl());
        UsuarioComunidad userComu;

        try {
            userComu = jdbcTemplate.queryForObject(UsuarioSql.USERCOMU_BY_COMU.toString(),
                    new UsuarioFullComunidadMapper(), userName, comunidadId);
        } catch (EmptyResultDataAccessException e) {
            userComu = null;
        }
        return userComu;
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
                    UsuarioSql.USUARIO_BY_EMAIL.toString(), new UsuarioMapper(), userName);
        } catch (EmptyResultDataAccessException e) {
            logger.error(e.getMessage());
            throw new UsernameNotFoundException(USER_NAME_NOT_FOUND.toString(), e);
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

    /**
     *  'Public' to allows for a userController test.
     */
    public int modifyUser(Usuario usuario)
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
        List<UsuarioComunidad> usuariosComunidad = jdbcTemplate.query(UsuarioSql.USERCOMUS_BY_COMU.toString(), new
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

    // .................. HELPER CLASSES ......................

    private static final class UsuarioMapper implements RowMapper<Usuario> {

        @Override
        public Usuario mapRow(ResultSet resultSet, int i) throws SQLException
        {
            return new Usuario.UsuarioBuilder().uId(resultSet.getLong("u_id"))
                    .userName(resultSet.getString("user_name"))
                    .password(resultSet.getString("password"))
                    .alias(resultSet.getString("alias"))
                    .build();
        }
    }

    private static final class UsuarioFullComunidadMapper implements RowMapper<UsuarioComunidad> {

        @Override
        public UsuarioComunidad mapRow(ResultSet resultSet, int i) throws SQLException
        {
            Usuario usuario = new Usuario.UsuarioBuilder()
                    .uId(resultSet.getLong("u_id"))
                    .userName(resultSet.getString("user_name"))
                    .alias(resultSet.getString("alias"))
                    .build();

            Comunidad comunidad = new Comunidad.ComunidadBuilder()
                    .c_id(resultSet.getLong("c_id"))
                    .tipoVia(resultSet.getString("tipo_via"))
                    .nombreVia(resultSet.getString("nombre_via"))
                    .numero(resultSet.getShort("numero"))
                    .sufijoNumero(resultSet.getString("sufijo_numero"))
                    .municipio(
                            new Municipio(
                                    resultSet.getShort("m_cd"),
                                    resultSet.getString("m_nombre"),
                                    new Provincia(
                                            resultSet.getShort("pr_id"),
                                            resultSet.getString("pr_nombre"))))
                    .build();

            return new UsuarioComunidad.UserComuBuilder(comunidad, usuario)
                    .portal(resultSet.getString("portal"))
                    .escalera(resultSet.getString("escalera"))
                    .planta(resultSet.getString("planta"))
                    .puerta(resultSet.getString("puerta"))
                    .roles(resultSet.getString("roles"))
                    .build();
        }
    }

    private static final class UsuarioComunidadMapper implements RowMapper<UsuarioComunidad> {

        @Override
        public UsuarioComunidad mapRow(ResultSet resultSet, int i) throws SQLException
        {
            Usuario usuario = new Usuario.UsuarioBuilder()
                    .uId(resultSet.getLong("u_id"))
                    .userName(resultSet.getString("user_name"))
                    .alias(resultSet.getString("alias"))
                    .build();

            Comunidad comunidad = new Comunidad.ComunidadBuilder()
                    .c_id(resultSet.getLong("c_id"))
                    .build();

            return new UsuarioComunidad.UserComuBuilder(comunidad, usuario)
                    .portal(resultSet.getString("portal"))
                    .escalera(resultSet.getString("escalera"))
                    .planta(resultSet.getString("planta"))
                    .puerta(resultSet.getString("puerta"))
                    .roles(resultSet.getString("roles"))
                    .build();
        }
    }

    private static class UsuarioMapperForGcm implements RowMapper<Usuario> {
        @Override
        public Usuario mapRow(ResultSet resultSet, int i) throws SQLException
        {
            return new Usuario.UsuarioBuilder().uId(resultSet.getLong("u_id"))
                    .userName(resultSet.getString("user_name"))
                    .alias(resultSet.getString("alias"))
                    .gcmToken(resultSet.getString("gcm_token"))
                    .build();
        }
    }
}