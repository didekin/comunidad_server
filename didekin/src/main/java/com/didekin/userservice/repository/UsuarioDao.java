package com.didekin.userservice.repository;

import com.didekin.common.repository.ServiceException;
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static com.didekin.userservice.repository.UsuarioSql.COMUS_BY_USER;
import static com.didekin.userservice.repository.UsuarioSql.DELETE_BY_NAME;
import static com.didekin.userservice.repository.UsuarioSql.DELETE_GCM_TOKEN;
import static com.didekin.userservice.repository.UsuarioSql.DELETE_USER_COMUNIDAD;
import static com.didekin.userservice.repository.UsuarioSql.GCM_TOKENS_BY_COMUNIDAD;
import static com.didekin.userservice.repository.UsuarioSql.INSERT;
import static com.didekin.userservice.repository.UsuarioSql.IS_USER_IN_COMUNIDAD;
import static com.didekin.userservice.repository.UsuarioSql.MODIFY_GCM_TOKEN_BY_TOKEN;
import static com.didekin.userservice.repository.UsuarioSql.MODIFY_USER;
import static com.didekin.userservice.repository.UsuarioSql.MODIFY_USERCOMU;
import static com.didekin.userservice.repository.UsuarioSql.MODIFY_USER_ALIAS;
import static com.didekin.userservice.repository.UsuarioSql.NEW_PASSWORD;
import static com.didekin.userservice.repository.UsuarioSql.OLDEST_USER_COMU;
import static com.didekin.userservice.repository.UsuarioSql.PK;
import static com.didekin.userservice.repository.UsuarioSql.ROLES_ALL_FUNC;
import static com.didekin.userservice.repository.UsuarioSql.UPDATE_TOKEN_AUTH_BY_ID;
import static com.didekin.userservice.repository.UsuarioSql.UPDATE_TOKEN_AUTH_BY_NAME;
import static com.didekin.userservice.repository.UsuarioSql.USERCOMUS_BY_COMU;
import static com.didekin.userservice.repository.UsuarioSql.USERCOMUS_BY_USER;
import static com.didekin.userservice.repository.UsuarioSql.USERCOMU_BY_COMU;
import static com.didekin.userservice.repository.UsuarioSql.USERCOMU_BY_EMAIL;
import static com.didekin.userservice.repository.UsuarioSql.USER_BY_EMAIL;
import static com.didekin.userservice.repository.UsuarioSql.USER_BY_ID;
import static com.didekinlib.http.usuario.UsuarioExceptionMsg.USERCOMU_WRONG_INIT;
import static com.didekinlib.http.usuario.UsuarioExceptionMsg.USER_COMU_NOT_FOUND;
import static com.didekinlib.http.usuario.UsuarioExceptionMsg.USER_NOT_FOUND;
import static java.lang.Boolean.TRUE;
import static java.sql.JDBCType.INTEGER;
import static java.util.Objects.requireNonNull;

/**
 * User: pedro
 * Date: 31/03/15
 * Time: 09:37
 */
public class UsuarioDao {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioDao.class.getCanonicalName());

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public UsuarioDao(JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    //    ============================================================
    //    ..........................  METHODS ........................
    //    ============================================================

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

    JdbcTemplate getJdbcTemplate()
    {
        return jdbcTemplate;
    }

    int deleteGcmToken(String originalGcmTk)
    {
        logger.info("deleteGcmToken(),jdbcUrl: " + requireNonNull(jdbcTemplate.getDataSource()).toString());
        return jdbcTemplate.update(DELETE_GCM_TOKEN.toString(), originalGcmTk);
    }

    boolean deleteUser(String userName) throws ServiceException
    {
        logger.info("deleteUser()");

        int rowsDeleted = jdbcTemplate.update(DELETE_BY_NAME.toString(), userName);
        if (!(rowsDeleted == 1)) {
            throw new ServiceException(USER_NOT_FOUND);
        }
        return TRUE;
    }

    int deleteUserComunidad(UsuarioComunidad usuarioComunidad) throws ServiceException
    {
        logger.info("deleteUserComunidad(),jdbcUrl: " + (requireNonNull(jdbcTemplate.getDataSource())).toString());

        Comunidad comunidad = usuarioComunidad.getComunidad();

        int rowsDeleted = jdbcTemplate.update(DELETE_USER_COMUNIDAD.toString(),
                comunidad.getC_Id(),
                usuarioComunidad.getUsuario().getuId());

        if (!(rowsDeleted == 1)) {
            throw new ServiceException(USER_COMU_NOT_FOUND);
        }
        return rowsDeleted;
    }

    List<String> getAllRolesFunctionalUser(String userName)
    {
        logger.info("getAllRolesFunctionalUser() ,jdbcUrl: " + (requireNonNull(jdbcTemplate.getDataSource())).toString());

        List<String> roles = jdbcTemplate.queryForList(
                ROLES_ALL_FUNC.toString(), String.class, userName);

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

    List<Comunidad> getComusByUser(String userName)     // TODO: fail.
    {
        logger.info("getComusByUser() ,jdbcUrl: " + (requireNonNull(jdbcTemplate.getDataSource())).toString());
        return jdbcTemplate.query(COMUS_BY_USER.toString(), new Object[]{userName},
                new ComunidadDao.ComunidadMapper());
    }

    List<String> getGcmTokensByComunidad(long comunidadId)
    {
        logger.debug("getGcmTokensByComunidad(), jdbcUrl: " + (requireNonNull(jdbcTemplate.getDataSource())).toString());
        return jdbcTemplate.queryForList(GCM_TOKENS_BY_COMUNIDAD.toString(), String.class, comunidadId);
    }

    long getMaxPk()
    {
        Integer maxPk = jdbcTemplate.queryForObject(UsuarioSql.MAX_PK.toString(), Integer.class);
        return (maxPk == null) ? -1L : maxPk;
    }

    long getOldestUserComuId(long comunidadId)
    {
        logger.debug("getOldestUserComuId(), jdbcUrl: " + requireNonNull(jdbcTemplate.getDataSource()).toString());

        return requireNonNull(jdbcTemplate.queryForObject(
                OLDEST_USER_COMU.toString(),
                new Long[]{comunidadId},
                (resultSet, rowNum) -> resultSet.getLong("u_id"))
        );
    }

    Usuario getUserDataByName(String userName)
    {
        logger.info("getUserData(), jdbcUrl: " + (requireNonNull(jdbcTemplate.getDataSource())).toString());

        Usuario usuario;
        try {
            usuario = jdbcTemplate.queryForObject(
                    USER_BY_EMAIL.toString(), new UsuarioMapper(), userName);
        } catch (EmptyResultDataAccessException e) {
            throw new ServiceException(USER_NOT_FOUND);
        }
        return usuario;
    }

    Usuario getUserDataById(long idUsuario) throws ServiceException
    {
        logger.info("getUserDataById(), jdbcUrl: " + (requireNonNull(jdbcTemplate.getDataSource())).toString());

        Usuario usuario;
        try {
            usuario = jdbcTemplate.queryForObject(USER_BY_ID.toString(),
                    new UsuarioMapper(), idUsuario);
        } catch (EmptyResultDataAccessException e) {
            logger.error(e.getMessage());
            throw new ServiceException(USER_NOT_FOUND);
        }
        return usuario;
    }

    /**
     * @return a fully initialized UsuarioComuidad instance or null if the pair usuario-comunidad is not in DB.
     */
    UsuarioComunidad getUserComuFullByUserAndComu(String userName, long comunidadId) throws ServiceException      // TODO: fail.
    {
        logger.debug("getUserComuFullByUserAndComu(), jdbcUrl: " + (requireNonNull(jdbcTemplate.getDataSource())).toString());
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
            throw new ServiceException(USERCOMU_WRONG_INIT);
        }
    }

    int modifyUserComu(UsuarioComunidad userComu)
    {
        logger.info("modifyUserComu()");

        return jdbcTemplate.update(MODIFY_USERCOMU.toString(),
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
        logger.info("insertUsuario(), jdbcUrl: " + (requireNonNull(jdbcTemplate.getDataSource())).toString());
        ResultSet rs;
        long usuarioId;

        try (PreparedStatement ps = conn.prepareStatement(INSERT.toString(), new String[]{PK.toString()})) {
            ps.setNull(1, INTEGER.getVendorTypeNumber());
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

    boolean isUserInComunidad(String userName, long comunidadId)
    {
        logger.debug("isUserInComunidad()");
        return requireNonNull(jdbcTemplate.queryForObject(
                IS_USER_IN_COMUNIDAD.toString(),
                new Object[]{userName, comunidadId},
                Integer.class)) > 0;
    }

    /**
     * 'Public' to allow for a userController test.
     */
    int modifyUser(Usuario usuario)
    {
        logger.debug("modifyUser(), jdbcUrl: " + requireNonNull(jdbcTemplate.getDataSource()).toString());

        return jdbcTemplate.update(MODIFY_USER.toString(),
                usuario.getAlias(),
                usuario.getUserName(),
                usuario.getPassword(),
                usuario.getuId());
    }

    int modifyUserAlias(Usuario user)
    {
        logger.debug("modifyUserAlias()");

        return jdbcTemplate.update(MODIFY_USER_ALIAS.toString(),
                user.getAlias(),
                user.getuId());
    }

    int modifyUserGcmToken(GcmTokensHolder holder)
    {
        logger.debug("modifyUserGcmToken()");
        return jdbcTemplate.update(MODIFY_GCM_TOKEN_BY_TOKEN.toString(), holder.getNewGcmTk(), holder.getOriginalGcmTk());
    }

    List<UsuarioComunidad> seeUserComusByComu(long idComunidad)     // TODO: fail.
    {
        logger.info("seeUserComusByComu(), jdbcUrl: " + (requireNonNull(jdbcTemplate.getDataSource())).toString());
        List<UsuarioComunidad> usuariosComunidad = jdbcTemplate.query(USERCOMUS_BY_COMU.toString(),
                new Object[]{idComunidad},
                new UsuarioComunidadMapper());
        logger.debug("seeUserComusByComu(); usuariosComunidad.size = " + usuariosComunidad.size());
        return usuariosComunidad;
    }

    int passwordChange(Usuario usuario)
    {
        logger.info("passwordChange(), jdbcUrl: " + (requireNonNull(jdbcTemplate.getDataSource())).toString());

        return jdbcTemplate.update(NEW_PASSWORD.toString(),
                usuario.getPassword(),
                usuario.getuId());
    }

    List<UsuarioComunidad> seeUserComusByUser(String userName)      // TODO: fail.
    {
        logger.info("seeUserComusByUser(), jdbcUrl: " + (requireNonNull(jdbcTemplate.getDataSource())).toString());

        return jdbcTemplate.query(
                USERCOMUS_BY_USER.toString(),
                new Object[]{userName},
                new UsuarioFullComunidadMapper());
    }

    /**
     * It updates the DB field token_auth with a new BCcripted authorization token.
     *
     * @return true if the data is updated.
     * @throws ServiceException (USER_NOT_FOUND) if the update does not return 1.
     */
    boolean updateTokenAuthById(long userId, String tokenAuthBCrypted)
    {
        logger.debug("updateTokenAuthById()");
        if (jdbcTemplate.update(UPDATE_TOKEN_AUTH_BY_ID.toString(), tokenAuthBCrypted, userId) == 1) {
            return TRUE;
        }
        throw new ServiceException(USER_NOT_FOUND);
    }

    boolean updateTokenAuthByUserName(String userName, String tokenAuthBCrypted)
    {
        logger.debug("updateTokenAuthByUserName()");
        if (jdbcTemplate.update(UPDATE_TOKEN_AUTH_BY_NAME.toString(), tokenAuthBCrypted, userName) == 1) {
            return TRUE;
        }
        throw new ServiceException(USER_NOT_FOUND);
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

    // .................. MAPPER CLASSES ......................

    private static final class UsuarioMapper implements RowMapper<Usuario> {

        @Override
        public Usuario mapRow(ResultSet rs, int i) throws SQLException
        {
            return new Usuario.UsuarioBuilder()
                    .uId(rs.getLong("u_id"))
                    .userName(rs.getString("user_name"))
                    .alias(rs.getString("alias"))
                    .password(rs.getString("password"))
                    .gcmToken(rs.getString("gcm_token"))
                    .tokenAuth(rs.getString("token_auth"))
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
}