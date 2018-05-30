package com.didekin.userservice.repository;

import com.didekin.common.repository.ServiceException;
import com.didekinlib.http.usuario.AuthHeader;
import com.didekinlib.model.comunidad.Comunidad;
import com.didekinlib.model.usuario.Usuario;
import com.didekinlib.model.usuariocomunidad.UsuarioComunidad;

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
    UserMockManager(UsuarioManager usuarioManagerIn)
    {
        usuarioManager = usuarioManagerIn;
        comunidadDao = usuarioManager.comunidadDao;
        usuarioDao = usuarioManager.usuarioDao;
    }

    public String insertTokenGetHeaderStr(String userName, String appIDIn)
    {
        String newTokenStr = updateTokenAuthInDb(userName, appIDIn);
        return new AuthHeader.AuthHeaderBuilder()
                .userName(userName)
                .appId(appIDIn)
                .tokenInLocal(newTokenStr)
                .build()
                .getBase64Str();
    }

    public boolean regComuAndUserAndUserComu(final UsuarioComunidad usuarioCom) throws ServiceException
    {
        logger.info("regComuAndUserAndUserComu()");
        checkActiveProfiles(env);

        final UsuarioComunidad userComEncryptPswd =
                new UsuarioComunidad.UserComuBuilder(usuarioCom.getComunidad(), doUserEncryptPswd(usuarioCom.getUsuario()))
                        .userComuRest(usuarioCom).build();

        long pkUsuario = 0;
        long pkComunidad = 0;
        int userComuInserted = 0;
        Connection conn = null;

        try {
            conn = comunidadDao.getJdbcTemplate().getDataSource().getConnection();
            conn.setAutoCommit(false);
            pkUsuario = usuarioDao.insertUsuario(userComEncryptPswd.getUsuario(), conn);
            pkComunidad = comunidadDao.insertComunidad(userComEncryptPswd.getComunidad(), conn);

            Usuario userWithPk = new Usuario.UsuarioBuilder().uId(pkUsuario).build();
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

        return pkUsuario > 0L && pkComunidad > 0L && userComuInserted == 1;
    }

    public boolean regUserAndUserComu(final UsuarioComunidad userComu) throws ServiceException
    {
        logger.debug("regUserAndUserComu()");
        checkActiveProfiles(env);

        final UsuarioComunidad userComEncryptPswd =
                new UsuarioComunidad.UserComuBuilder(userComu.getComunidad(), doUserEncryptPswd(userComu.getUsuario()))
                        .userComuRest(userComu).build();

        long pkUsuario = 0;
        int userComuInserted = 0;
        Connection conn = null;

        try {
            conn = usuarioDao.getJdbcTemplate().getDataSource().getConnection();
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            conn.setAutoCommit(false);
            pkUsuario = usuarioDao.insertUsuario(userComEncryptPswd.getUsuario(), conn);
            final Usuario usuarioPk = new Usuario.UsuarioBuilder().uId(pkUsuario).build();
            final UsuarioComunidad userComuTris = new UsuarioComunidad.UserComuBuilder(
                    userComu.getComunidad(), usuarioPk)
                    .userComuRest(userComu)
                    .build();
            userComuInserted = comunidadDao.insertUsuarioComunidad(userComuTris, conn);
            conn.commit();
        } catch (SQLException se) {
            doCatchSqlException(conn, se);
        } finally {
            doFinallyJdbc(conn, "regUserAndUserComu(): conn.setAutoCommit(true), conn.close(): ");
        }
        return pkUsuario > 0L && userComuInserted == 1;
    }

    // ...................................  HELPERS  .........................................

    private String updateTokenAuthInDb(String userName, String appId)
    {
        String tokenAuthStr = usuarioManager.producerBuilder.defaultHeadersClaims(userName, appId).build().getEncryptedTkStr();
        return usuarioDao.updateTokenAuthByUserName(userName, hashpw(tokenAuthStr, BCRYPT_SALT.get())) ? tokenAuthStr : null;
    }
}