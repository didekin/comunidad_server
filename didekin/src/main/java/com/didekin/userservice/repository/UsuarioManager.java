package com.didekin.userservice.repository;

import com.didekin.common.EntityException;
import com.didekin.userservice.mail.UsuarioMailService;
import com.didekinlib.gcm.model.common.GcmTokensHolder;
import com.didekinlib.model.common.dominio.ValidDataPatterns;
import com.didekinlib.model.comunidad.Comunidad;
import com.didekinlib.model.usuario.Usuario;
import com.didekinlib.model.usuariocomunidad.UsuarioComunidad;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.mail.MailException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.didekin.userservice.repository.PswdGenerator.GENERATOR_13;
import static com.didekinlib.http.GenericExceptionMsg.TOKEN_NOT_DELETED;
import static com.didekinlib.http.GenericExceptionMsg.UNAUTHORIZED_TX_TO_USER;
import static com.didekinlib.http.UsuarioServConstant.IS_USER_DELETED;
import static com.didekinlib.model.comunidad.ComunidadExceptionMsg.COMUNIDAD_DUPLICATE;
import static com.didekinlib.model.comunidad.ComunidadExceptionMsg.COMUNIDAD_NOT_FOUND;
import static com.didekinlib.model.usuario.UsuarioExceptionMsg.PASSWORD_NOT_SENT;
import static com.didekinlib.model.usuario.UsuarioExceptionMsg.USER_DATA_NOT_MODIFIED;
import static com.didekinlib.model.usuario.UsuarioExceptionMsg.USER_NAME_DUPLICATE;
import static com.didekinlib.model.usuario.UsuarioExceptionMsg.USER_NAME_NOT_FOUND;
import static com.didekinlib.model.usuario.UsuarioExceptionMsg.USER_WRONG_INIT;
import static com.didekinlib.model.usuariocomunidad.Rol.getRolFromFunction;
import static com.didekinlib.model.usuariocomunidad.UsuarioComunidadExceptionMsg.USERCOMU_WRONG_INIT;
import static com.google.common.base.Preconditions.checkArgument;

/**
 * User: pedro@didekin
 * Date: 20/04/15
 * Time: 15:41
 */
@SuppressWarnings("SpringAutowiredFieldsWarningInspection")
@Service
public class UsuarioManager implements UsuarioManagerIf {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioManager.class.getCanonicalName());


    private final ComunidadDao comunidadDao;
    private final UsuarioDao usuarioDao;
    @Autowired
    private JdbcTokenStore tokenStore;
    @Autowired
    private UsuarioMailService usuarioMailService;

    @Autowired
    UsuarioManager(ComunidadDao comunidadDao, UsuarioDao usuarioDao)
    {
        this.comunidadDao = comunidadDao;
        this.usuarioDao = usuarioDao;
    }

    @Override
    public UsuarioDao getUsuarioDao()
    {
        return usuarioDao;
    }

    //    ============================================================
    //    .......... UsuarioManagerIf .......
    //    ============================================================

    @Override
    public Usuario completeUser(String userName) throws EntityException
    {
        logger.debug("completeUser()");
        return new Usuario.UsuarioBuilder()
                .copyUsuario(getUserByUserName(userName))
                .password(null)
                .build();
    }

    @Override
    public UsuarioComunidad completeWithUserComuRoles(String userName, long comunidadId) throws EntityException
    {
        logger.debug("completeWithUserComuRoles()");
        return usuarioDao.getUserComuRolesByUserName(userName, comunidadId);
    }

    @Override
    public boolean deleteAccessToken(String accessTkValue) throws EntityException
    {
        logger.debug("deleteAccessToken()");
        tokenStore.removeAccessToken(accessTkValue);
        if (getAccessToken(accessTkValue) != null) {
            throw new EntityException(TOKEN_NOT_DELETED);
        }
        return true;
    }

    @Override
    public boolean deleteAccessTokenByUserName(String userName) throws EntityException
    {
        logger.debug("deleteAccessTokenByUserName()");
        checkArgument(userName != null);
        Optional<OAuth2AccessToken> oAuthTkOptional = getAccessTokenByUserName(userName);
        return oAuthTkOptional.isPresent() && deleteAccessToken(oAuthTkOptional.get().getValue());
    }

    @Override
    public boolean deleteUser(String userName) throws EntityException
    {
        logger.info("deleteUser()");
        return deleteUserAndComunidades(userName) >= 1;
    }

    @Override
    public int deleteUserAndComunidades(String userName) throws EntityException
    {
        logger.info("deleteUserAndComunidades()");
        List<UsuarioComunidad> comunidades = usuarioDao.seeUserComusByUser(userName);

        int contador = 0;
        for (UsuarioComunidad comunidade : comunidades) {
            deleteUserComunidad(comunidade);
            ++contador;
        }
        return contador;
    }

    @Override
    public int deleteUserComunidad(final UsuarioComunidad usuarioComunidad) throws EntityException
    {
        logger.info("deleteUserComunidad()");

        final Comunidad comunidad = usuarioComunidad.getComunidad();
        final String userName = usuarioComunidad.getUsuario().getUserName();

        int rowsDeleted = usuarioDao.deleteUserComunidad(usuarioComunidad);

        if (rowsDeleted == 1 && comunidadDao.getUsuariosIdFromComunidad(comunidad).size() == 0) {
            logger.info("deleteUserComunidad(); comunidad is also deleted");
            comunidadDao.deleteComunidad(comunidad);
        }

        if (rowsDeleted == 1 && usuarioDao.seeUserComusByUser(userName).size() == 0) {
            logger.info("deleteUserComunidad(); usuario is also deleted");
            rowsDeleted = usuarioDao.deleteUser(userName) ? IS_USER_DELETED : rowsDeleted;
        }
        return rowsDeleted;
    }

    @Override
    public OAuth2AccessToken getAccessToken(String accesTkValue)
    {
        logger.debug("getAccessToken()");
        return tokenStore.readAccessToken(accesTkValue);
    }

    @Override
    public Optional<OAuth2AccessToken> getAccessTokenByUserName(String userName)
    {
        logger.debug("getAccessTokenByUserName()");
        return tokenStore.findTokensByUserName(userName)
                .stream()
                .filter(oAuth2Token -> oAuth2Token.getValue() != null)
                .findFirst();
    }

    @Override
    public Comunidad getComunidadById(long comunidadId) throws EntityException
    {
        logger.info("getComunidadById()");
        return comunidadDao.getComunidadById(comunidadId);
    }

    @Override
    public Comunidad getComunidadById(Usuario user, long comunidadId) throws EntityException
    {
        logger.debug("getComunidadById()");
        if (comunidadDao.existsUserComu(comunidadId, user.getuId())) {
            return comunidadDao.getComunidadById(comunidadId);
        } else {
            throw new EntityException(USERCOMU_WRONG_INIT);
        }
    }

    @Override
    public List<Comunidad> getComusByUser(String userName)
    {
        logger.debug("getComusByUser()");
        return usuarioDao.getComusByUser(userName);
    }

    @Override
    public String getGcmToken(long usuarioId)
    {
        String token = usuarioDao.getUsuarioWithGcmToken(usuarioId).getGcmToken();
        return token != null ? token.trim() : token;
    }

    @Override
    public List<String> getGcmTokensByComunidad(long comunidadId)
    {
        logger.debug("getGcmTokensByComunidad()");
        return Stream.of(usuarioDao.getGcmTokensByComunidad(comunidadId))
                .peek(tokensList -> logger.debug("getGcmTokensByComunidad(); gcmTokens size = " + tokensList.size()))
                .findFirst().get();
    }

    @Override
    public List<String> getRolesSecurity(Usuario usuario)
    {
        logger.info("getRolesSecurity()");

        List<String> functionalRoles = usuarioDao.getAllRolesFunctionalUser(usuario.getUserName());
        List<String> authorities = new ArrayList<>();

        for (String functionalRole : functionalRoles) {
            final String authority = getRolFromFunction(functionalRole).authority;
            if (!authorities.contains(authority)) {
                authorities.add(authority);
            }
        }
        return authorities;
    }

    /**
     * @return null if the pair usuarioComunidad doesn't exist.
     */
    @Override
    public UsuarioComunidad getUserComuByUserAndComu(String userName, long comunidadId) throws EntityException
    {
        logger.debug("getUserComuFullByUserAndComu()");
        return usuarioDao.getUserComuFullByUserAndComu(userName, comunidadId);
    }

    @Override
    public Usuario getUserByUserName(String email) throws EntityException
    {
        logger.info("getUserByUserName()");
        try {
            return usuarioDao.getUserByUserName(email);
        } catch (UsernameNotFoundException e) {
            throw new EntityException(USER_NAME_NOT_FOUND);
        }
    }

    @Override
    public boolean isOldestUserComu(Usuario user, long comunidadId) throws EntityException
    {
        logger.debug("isOldestOrAdmonUserComu()");
        long idOldestUser = 0;
        try {
            idOldestUser = usuarioDao.getOldestUserComuId(comunidadId);
        } catch (Exception e) {
            if (e instanceof EmptyResultDataAccessException) {
                throw new EntityException(COMUNIDAD_NOT_FOUND);
            }
        }
        return user.getuId() == idOldestUser;
    }

    /**
     * @return false if the userName exists but the password doesn't match that in the data base.
     * @throws EntityException if  USER_WRONG_INIT or USER_NAME_NOT_FOUND.
     */
    @Override
    public boolean login(Usuario usuario) throws EntityException
    {
        logger.debug("login()");

        if (!ValidDataPatterns.EMAIL.isPatternOk(usuario.getUserName())) {
            throw new EntityException(USER_WRONG_INIT);
        }

        Usuario usuarioDb;
        try {
            usuarioDb = getUserByUserName(usuario.getUserName());
        } catch (UsernameNotFoundException e) {
            throw new EntityException(USER_NAME_NOT_FOUND);
        }

        return new BCryptPasswordEncoder().matches(usuario.getPassword(), usuarioDb.getPassword());
    }

    @Override
    public String makeNewPassword() throws EntityException
    {
        logger.debug("makeNewPassword()");
        String newPasssword = GENERATOR_13.makePswd();
        if (newPasssword.isEmpty()) {
            throw new EntityException(PASSWORD_NOT_SENT);
        }
        return newPasssword;
    }

    @Override
    public int modifyComuData(Usuario user, Comunidad comunidad) throws EntityException
    {
        logger.info("modifyComuData()");
        if (checkComuDataModificationPower(user, comunidad)) {
            return comunidadDao.modifyComuData(comunidad);
        }
        throw new EntityException(UNAUTHORIZED_TX_TO_USER);
    }

    /**
     * Preconditions:
     * 1. OldUserName cannot be null.
     * 2. UsuarioId cannot be null.
     * Postconditions:
     * 1. the userName and/or alias have been modified.
     * 2. if userName has been modified, user's accessToken has been deleted.
     *
     * @return number of rows afected in user table (it should be 1).
     * @throws EntityException if both newUserName and newAlias are both null.
     */
    @Override
    public int modifyUser(final Usuario usuario, String oldUserName) throws EntityException
    {
        logger.info("modifyUser()");

        int userModified = 0;
        boolean isAliasToModified = false;
        String newUserName = usuario.getUserName();
        Usuario newUsuario;
        Usuario.UsuarioBuilder newUsuarioBuilder =
                new Usuario.UsuarioBuilder().copyUsuario(usuarioDao.getUsuarioById(usuario.getuId()));

        if (usuario.getAlias() != null && !usuario.getAlias().isEmpty()) {
            newUsuarioBuilder.alias(usuario.getAlias());
            isAliasToModified = true;
        }

        if (newUserName != null && !newUserName.isEmpty()) {
            newUsuario = newUsuarioBuilder.userName(newUserName).build();
            userModified = usuarioDao.modifyUser(newUsuario);
            if (userModified > 0) {
                // Change of userName has been propagated to oauth_token table.
                deleteAccessTokenByUserName(newUserName);
            }
        } else if (isAliasToModified) {
            newUsuario = newUsuarioBuilder.build();
            userModified = usuarioDao.modifyUserAlias(newUsuario);
        }

        if (userModified <= 0) {
            throw new EntityException(USER_DATA_NOT_MODIFIED);
        }
        return userModified;
    }

    @Override
    public int modifyUserGcmToken(Usuario usuario)
    {
        logger.info("modifyUserGcmToken(Usuario usuario)");
        return usuarioDao.modifyUserGcmToken(usuario);
    }

    @Override
    public int modifyUserGcmToken(String userName, String gcmToken) throws EntityException
    {
        logger.debug("modifyUserGcmToken(String userName, String gcmToken)");
        Usuario usuario = new Usuario.UsuarioBuilder().uId(completeUser(userName).getuId()).gcmToken(gcmToken).build();
        return modifyUserGcmToken(usuario);
    }

    @Override
    public int modifyUserGcmTokens(List<GcmTokensHolder> holdersList)
    {
        logger.debug("modifyUserGcmToken(List<GcmTokensHolder> holdersList)");
        return (int) holdersList.parallelStream()
                .filter(holder -> holder.getOriginalGcmTk() != null)
                .map(holder -> holder.getNewGcmTk() == null ? usuarioDao.deleteGcmToken(holder.getOriginalGcmTk()) : usuarioDao.modifyUserGcmToken(holder))
                .count();
    }

    @Override
    public int modifyUserComu(UsuarioComunidad userComu)
    {
        logger.info("modifyUserComu()");
        return usuarioDao.modifyUserComu(userComu);
    }

    /**
     * Preconditions:
     * 1. userName and password must be not null and valid.
     * Postconditions:
     * 1. the password has been modified.
     * 2. if password has been modified, user's accessToken has been deleted.
     *
     * @return number of rows afected in user table: 1.
     * @throws EntityException if rows affected != 1.
     */
    @Override
    public int passwordChangeWithName(String userName, final String newPassword) throws EntityException
    {
        logger.info("passwordChangeWithName()");

        if (!ValidDataPatterns.EMAIL.isPatternOk(userName) || !ValidDataPatterns.PASSWORD.isPatternOk(newPassword)) {
            throw new EntityException(USER_WRONG_INIT);
        }

        final Usuario usuarioOld = usuarioDao.getUserByUserName(userName);
        return passwordChangeWithUser(usuarioOld);
    }

    @Override
    public int passwordChangeWithUser(Usuario usuarioNewPswd) throws EntityException
    {
        logger.info("passwordChangeWithUser()");

        final Usuario usuarioNew = new Usuario.UsuarioBuilder()
                .copyUsuario(usuarioNewPswd)
                .password(new BCryptPasswordEncoder().encode(usuarioNewPswd.getPassword()))
                .build();

        if (usuarioDao.passwordChange(usuarioNew) == 1) {
            deleteAccessTokenByUserName(usuarioNew.getUserName());
            return 1;
        } else {
            throw new EntityException(USER_DATA_NOT_MODIFIED);
        }
    }

    @Override
    public boolean passwordSend(String userName, String localeToStr) throws EntityException
    {
        logger.debug("passwordSend()");
        Usuario usuarioNewPswd = new Usuario.UsuarioBuilder()
                .copyUsuario(getUserByUserName(userName))
                .password(makeNewPassword())
                .build();
        return passwordSendDoMail(usuarioNewPswd, localeToStr);
    }

    @Override
    public boolean passwordSendDoMail(Usuario usuario, String localeToStr) throws EntityException
    {
        logger.debug("passwordSendDoMail()");
        try {
            usuarioMailService.sendNewPswd(usuario, localeToStr);  // TODO: hacer asíncrono.  // TODO: pasar resourceBundle.
        } catch (MailException e) {
            throw new EntityException(PASSWORD_NOT_SENT);
        }
        return passwordChangeWithUser(usuario) == 1;
    }

    @SuppressWarnings("Duplicates")
    @Override
    public boolean regComuAndUserAndUserComu(final UsuarioComunidad usuarioCom, String localeToStr) throws EntityException
    {
        logger.info("regComuAndUserAndUserComu()");

        // TODO: verificar que no tiene cubierto password.

        // Password generation and encryption.
        Usuario usuarioToDB = new Usuario.UsuarioBuilder()
                .copyUsuario(usuarioCom.getUsuario())
                .password(new BCryptPasswordEncoder().encode(makeNewPassword()))
                .build();
        UsuarioComunidad usuarioComToDB = new UsuarioComunidad.UserComuBuilder(usuarioCom.getComunidad(), usuarioToDB)
                .userComuRest(usuarioCom).build();

        long pkUsuario = 0;
        long pkComunidad = 0;
        int userComuInserted = 0;
        Connection conn = null;

        try {
            conn = comunidadDao.getJdbcTemplate().getDataSource().getConnection();
            conn.setAutoCommit(false);
            pkUsuario = usuarioDao.insertUsuario(usuarioComToDB.getUsuario(), conn);
            pkComunidad = comunidadDao.insertComunidad(usuarioComToDB.getComunidad(), conn);

            Usuario userWithPk = new Usuario.UsuarioBuilder().uId(pkUsuario).build();
            Comunidad comuWithPk = new Comunidad.ComunidadBuilder().c_id(pkComunidad).build();
            UsuarioComunidad userComuWithPks = new UsuarioComunidad.UserComuBuilder(comuWithPk, userWithPk)
                    .userComuRest(usuarioComToDB).build();

            userComuInserted = comunidadDao.insertUsuarioComunidad(userComuWithPks, conn);
            conn.commit();
        } catch (SQLException se) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
                if (se.getMessage().contains(EntityException.DUPLICATE_ENTRY) && se.getMessage().contains(EntityException.USER_NAME)) {
                    throw new EntityException(USER_NAME_DUPLICATE);
                }
                if (se.getMessage().contains(EntityException.DUPLICATE_ENTRY) && se.getMessage().contains(EntityException.COMUNIDAD_UNIQUE_KEY)) {
                    throw new EntityException(COMUNIDAD_DUPLICATE);
                }
            } catch (SQLException e) {
                throw new RuntimeException(se.getMessage(), se);
            }
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("regComuAndUserAndUserComu(): " + e.getMessage());
            }
        }
        passwordSendDoMail(usuarioToDB, localeToStr);    // TODO: test.
        return pkUsuario > 0L && pkComunidad > 0L && userComuInserted == 1;
    }

    @Override
    public boolean regComuAndUserComu(UsuarioComunidad usuarioCom) throws EntityException
    {
        logger.info("regComuAndUserComu()");

        Usuario usuario = usuarioCom.getUsuario();
        long pkComunidad = 0;
        int userComuInserted = 0;
        Connection conn = null;

        try {
            conn = comunidadDao.getJdbcTemplate().getDataSource().getConnection();
            conn.setAutoCommit(false);
            pkComunidad = comunidadDao.insertComunidad(usuarioCom.getComunidad(), conn);

            Comunidad comuWithPk = new Comunidad.ComunidadBuilder().c_id(pkComunidad).build();
            UsuarioComunidad userComuWithPks = new UsuarioComunidad.UserComuBuilder(comuWithPk, usuario)
                    .userComuRest(usuarioCom).build();
            userComuInserted = comunidadDao.insertUsuarioComunidad(userComuWithPks, conn);
            conn.commit();
        } catch (SQLException se) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
                if (se.getMessage().contains(EntityException.DUPLICATE_ENTRY) && se.getMessage().contains(EntityException.COMUNIDAD_UNIQUE_KEY)) {
                    throw new EntityException(COMUNIDAD_DUPLICATE);
                }
            } catch (SQLException e) {
                throw new RuntimeException(se.getMessage(), se);
            }
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("regComuAndUserComu(): conn.setAutoCommit(true), conn.close(): " + e.getMessage());
            }
        }
        return pkComunidad > 0L && userComuInserted == 1;
    }

    @Override
    public boolean regUserAndUserComu(final UsuarioComunidad userComu, String localeToStr) throws EntityException
    {
        logger.debug("regUserAndUserComu()");

        // TODO: verificar que no tiene cubierto password.

        // Password generation and encryption.
        final Usuario usuarioToDB = new Usuario.UsuarioBuilder()
                .copyUsuario(userComu.getUsuario())
                .password(new BCryptPasswordEncoder().encode(makeNewPassword()))
                .build();
        final UsuarioComunidad usuarioComToDB = new UsuarioComunidad.UserComuBuilder(userComu.getComunidad(), usuarioToDB)
                .userComuRest(userComu)
                .build();

        long pkUsuario = 0;
        int userComuInserted = 0;
        Connection conn = null;

        try {
            conn = usuarioDao.getJdbcTemplate().getDataSource().getConnection();
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            conn.setAutoCommit(false);
            pkUsuario = usuarioDao.insertUsuario(usuarioComToDB.getUsuario(), conn);
            final Usuario usuarioPk = new Usuario.UsuarioBuilder().uId(pkUsuario).build();
            final UsuarioComunidad userComuTris = new UsuarioComunidad.UserComuBuilder(
                    userComu.getComunidad(), usuarioPk)
                    .userComuRest(userComu)
                    .build();
            userComuInserted = comunidadDao.insertUsuarioComunidad(userComuTris, conn);
            conn.commit();
        } catch (SQLException se) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
                if (se.getMessage().contains(EntityException.DUPLICATE_ENTRY) && se.getMessage().contains(EntityException.USER_NAME)) {
                    throw new EntityException(USER_NAME_DUPLICATE);
                }
            } catch (SQLException e) {
                throw new RuntimeException(se.getMessage(), se);
            }
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("regUserAndUserComu(): conn.setAutoCommit(true), conn.close(): " + e.getMessage());
            }
        }
        passwordSendDoMail(usuarioToDB, localeToStr); // TODO: test.
        return pkUsuario > 0L && userComuInserted == 1;
    }

    @Override
    public int regUserComu(UsuarioComunidad usuarioComunidad)
    {
        logger.info("regUserComu()");
        return comunidadDao.insertUsuarioComunidad(usuarioComunidad);
    }

    @Override
    public List<Comunidad> searchComunidades(Comunidad comunidad)
    {
        logger.info("searchComunidades()");

        List<Comunidad> comunidades;

        if ((comunidades = comunidadDao.searchComunidadOne(comunidad)).size() > 0) {
            return comunidades;
        }
        if ((comunidades = comunidadDao.searchComunidadTwo(comunidad)).size() > 0) {
            return comunidades;
        }

        return comunidadDao.searchComunidadThree(comunidad);
    }

    @Override
    public List<UsuarioComunidad> seeUserComusByComu(long idComunidad)
    {
        logger.debug("seeUserComusByComu()");
        return usuarioDao.seeUserComusByComu(idComunidad);
    }

    @Override
    public List<UsuarioComunidad> seeUserComusByUser(String userName)
    {
        logger.info("seeUserComusByUser()");
        return usuarioDao.seeUserComusByUser(userName);
    }

    // =================================  CHECKERS ======================================

    /**
     * The method checks if a user is the oldest one in the comunidad or has the authority 'adm'.
     *
     * @param user      : user in session.
     * @param comunidad : comunidad to be modified.
     */
    @Override
    public boolean checkComuDataModificationPower(Usuario user, Comunidad comunidad) throws EntityException
    {
        logger.debug("checkIncidModificationPower()");
        return isOldestUserComu(user, comunidad.getC_Id()) || completeWithUserComuRoles(user.getUserName(), comunidad.getC_Id()).hasAdministradorAuthority();
    }
}