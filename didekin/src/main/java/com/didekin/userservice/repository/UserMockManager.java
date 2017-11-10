package com.didekin.userservice.repository;

import com.didekin.common.EntityException;
import com.didekinlib.model.comunidad.Comunidad;
import com.didekinlib.model.usuario.Usuario;
import com.didekinlib.model.usuariocomunidad.UsuarioComunidad;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;

import static com.didekin.common.Profiles.NGINX_JETTY_LOCAL;
import static com.didekin.common.Profiles.NGINX_JETTY_PRE;
import static com.didekin.userservice.repository.UsuarioManager.doCatchSqlException;
import static com.didekin.userservice.repository.UsuarioManager.doFinallyJdbc;
import static com.didekin.userservice.repository.UsuarioManager.doUserEncryptPswd;

/**
 * User: pedro@didekin
 * Date: 20/04/15
 * Time: 15:41
 */
@Profile({NGINX_JETTY_PRE, NGINX_JETTY_LOCAL})
@Service
public class UserMockManager {

    private static final Logger logger = LoggerFactory.getLogger(UserMockManager.class.getCanonicalName());

    private final ComunidadDao comunidadDao;
    private final UsuarioDao usuarioDao;

    @Autowired
    UserMockManager(ComunidadDao comunidadDao, UsuarioDao usuarioDao)
    {
        this.comunidadDao = comunidadDao;
        this.usuarioDao = usuarioDao;
    }

    public boolean regComuAndUserAndUserComu(final UsuarioComunidad usuarioCom) throws EntityException
    {
        logger.info("regComuAndUserAndUserComu()");

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

    public boolean regUserAndUserComu(final UsuarioComunidad userComu) throws EntityException
    {
        logger.debug("regUserAndUserComu()");

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
}