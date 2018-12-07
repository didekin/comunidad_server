package com.didekin.userservice.repository;

import com.didekin.common.repository.ServiceException;
import com.didekinlib.model.entidad.comunidad.Comunidad;
import com.didekinlib.model.relacion.usuariocomunidad.UsuarioComunidad;
import com.didekinlib.model.usuario.Usuario;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;

import static com.didekin.common.springprofile.Profiles.NGINX_JETTY_LOCAL;
import static com.didekin.common.springprofile.Profiles.NGINX_JETTY_PRE;
import static com.didekin.common.springprofile.Profiles.checkActiveProfiles;
import static com.didekin.userservice.repository.UsuarioManager.BCRYPT_SALT;
import static com.didekin.userservice.repository.UsuarioManager.doCatchSqlException;
import static com.didekin.userservice.repository.UsuarioManager.doFinallyJdbc;
import static com.didekin.userservice.repository.UsuarioManager.doUserEncryptPswd;
import static java.util.Objects.requireNonNull;
import static org.mindrot.jbcrypt.BCrypt.hashpw;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * User: pedro@didekin
 * Date: 20/04/15
 * Time: 15:41
 * <p>
 * Manager to be used mainly in conjunction with UserComuMockController, although it can be used with other controllers.
 */
@Profile({NGINX_JETTY_PRE, NGINX_JETTY_LOCAL})
@Service
public class UserMockManager {

    private static final Logger logger = getLogger(UserMockManager.class.getCanonicalName());

    private final ComunidadDao comunidadDao;
    private final UsuarioDao usuarioDao;
    private final UsuarioManager usuarioManager;

    @Autowired
    Environment env;

    @Autowired
    public UserMockManager(UsuarioManager usuarioManagerIn)
    {
        usuarioManager = usuarioManagerIn;
        comunidadDao = usuarioManager.comunidadDao;
        usuarioDao = usuarioManager.usuarioDao;
    }

    public String insertAuthTkGetNewAuthTkStr(String userName)
    {
        logger.debug("insertAuthTkGetNewAuthTkStr()");
        return updateTokenAuthInDb(userName);
    }

    public String regComuAndUserAndUserComu(final UsuarioComunidad usuarioCom) throws ServiceException
    {
        logger.info("regComuAndUserAndUserComu()");
        checkActiveProfiles(env);

        // Keep the password passed in the original usuario.
        final UsuarioComunidad userComEncryptPswd =
                new UsuarioComunidad.UserComuBuilder(usuarioCom.getEntidad(), doUserEncryptPswd(usuarioCom.getUsuario()))
                        .userComuRest(usuarioCom).build();

        long pkUsuario = 0;
        long pkComunidad = 0;
        int userComuInserted = 0;
        Usuario userWithPk;
        Connection conn = null;

        try {
            conn = requireNonNull(comunidadDao.getJdbcTemplate().getDataSource()).getConnection();
            conn.setAutoCommit(false);
            pkUsuario = usuarioDao.insertUsuario(userComEncryptPswd.getUsuario(), conn);
            pkComunidad = comunidadDao.insertComunidad(userComEncryptPswd.getEntidad(), conn);

            userWithPk = new Usuario.UsuarioBuilder().uId(pkUsuario).build();
            Comunidad comuWithPk = new Comunidad.ComunidadBuilder().c_id(pkComunidad).build();
            UsuarioComunidad userComuWithPks = new UsuarioComunidad.UserComuBuilder(comuWithPk, userWithPk)
                    .userComuRest(userComEncryptPswd).build();

            userComuInserted = comunidadDao.insertUsuarioComunidad(userComuWithPks, conn);
            conn.commit();
        } catch (SQLException se) {
            doCatchSqlException(conn, se);
        } finally {
            doFinallyJdbc(conn, "regComuAndUserAndUserComu(): ");
        }

        // Return new authTokenStr. The parameter usuarioCom contains an usuario withh gcmToken.
        return (pkUsuario > 0L && pkComunidad > 0L && userComuInserted == 1) ?
                usuarioManager.updateUserTokensInDb(
                        new Usuario.UsuarioBuilder()
                                .copyUsuario(usuarioCom.getUsuario())
                                .uId(pkUsuario)  // this is what is needed this new instance.
                                .build()
                ) :
                null;
    }

    public String regUserAndUserComu(final UsuarioComunidad userComu) throws ServiceException
    {
        logger.debug("regUserAndUserComu()");
        checkActiveProfiles(env);

        // Keep the password passed in the original usuario.
        final UsuarioComunidad userComEncryptPswd =
                new UsuarioComunidad.UserComuBuilder(userComu.getEntidad(), doUserEncryptPswd(userComu.getUsuario()))
                        .userComuRest(userComu).build();

        long pkUsuario = 0;
        Usuario usuarioPk;
        int userComuInserted = 0;
        Connection conn = null;

        try {
            conn = requireNonNull(usuarioDao.getJdbcTemplate().getDataSource()).getConnection();
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            conn.setAutoCommit(false);
            pkUsuario = usuarioDao.insertUsuario(userComEncryptPswd.getUsuario(), conn);
            usuarioPk = new Usuario.UsuarioBuilder().uId(pkUsuario).build();
            final UsuarioComunidad userComuTris = new UsuarioComunidad.UserComuBuilder(
                    userComu.getEntidad(), usuarioPk)
                    .userComuRest(userComu)
                    .build();
            userComuInserted = comunidadDao.insertUsuarioComunidad(userComuTris, conn);
            conn.commit();
        } catch (SQLException se) {
            doCatchSqlException(conn, se);
        } finally {
            doFinallyJdbc(conn, "regUserAndUserComu(): conn.setAutoCommit(true), conn.close(): ");
        }
        // Return new authTokenStr. The parameter usuarioCom contains an usuario withh gcmToken.
        return (pkUsuario > 0L && userComuInserted == 1) ?
                usuarioManager.updateUserTokensInDb(
                        new Usuario.UsuarioBuilder()
                                .copyUsuario(userComu.getUsuario())
                                .uId(pkUsuario)  // this is what is needed this new instance.
                                .build()
                )
                : null;
    }

    // ...................................  HELPERS  .........................................

    private String updateTokenAuthInDb(String userName)
    {
        logger.debug("updateUserTokensInDb()");
        String tokenAuthStr = usuarioManager.producerBuilder.defaultHeadersClaims(userName).build().getEncryptedTkStr();
        return usuarioDao.updateTokenAuthByUserName(userName, hashpw(tokenAuthStr, BCRYPT_SALT.get())) ? tokenAuthStr : null;
    }
}