package com.didekin.userservice.repository;

import com.didekin.common.auth.EncrypTkConsumerBuilder;
import com.didekin.common.repository.ServiceException;
import com.didekin.userservice.auth.EncrypTkProducerBuilder;
import com.didekin.userservice.mail.UsuarioMailService;
import com.didekin.userservice.mail.UsuarioMailServiceIf;
import com.didekinlib.gcm.GcmTokensHolder;
import com.didekinlib.model.comunidad.Comunidad;
import com.didekinlib.model.usuario.Usuario;
import com.didekinlib.model.usuario.http.AuthHeaderToken;
import com.didekinlib.model.usuariocomunidad.UsuarioComunidad;

import org.jose4j.jwt.MalformedClaimException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.mail.MailException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import static com.didekin.common.repository.ServiceException.COMUNIDAD_UNIQUE_KEY;
import static com.didekin.common.repository.ServiceException.DUPLICATE_ENTRY;
import static com.didekin.common.repository.ServiceException.GCM_TOKEN_KEY;
import static com.didekin.common.repository.ServiceException.USER_NAME;
import static com.didekinlib.http.exception.GenericExceptionMsg.DATABASE_ERROR;
import static com.didekinlib.model.common.dominio.ValidDataPatterns.EMAIL;
import static com.didekinlib.model.common.dominio.ValidDataPatterns.PASSWORD;
import static com.didekinlib.model.comunidad.http.ComunidadExceptionMsg.COMUNIDAD_DUPLICATE;
import static com.didekinlib.model.comunidad.http.ComunidadExceptionMsg.COMUNIDAD_NOT_FOUND;
import static com.didekinlib.model.usuario.http.TkValidaPatterns.tkEncrypted_direct_symmetricKey_REGEX;
import static com.didekinlib.model.usuario.http.UsuarioExceptionMsg.PASSWORD_NOT_SENT;
import static com.didekinlib.model.usuario.http.UsuarioExceptionMsg.PASSWORD_WRONG;
import static com.didekinlib.model.usuario.http.UsuarioExceptionMsg.UNAUTHORIZED;
import static com.didekinlib.model.usuario.http.UsuarioExceptionMsg.UNAUTHORIZED_TX_TO_USER;
import static com.didekinlib.model.usuario.http.UsuarioExceptionMsg.USERCOMU_WRONG_INIT;
import static com.didekinlib.model.usuario.http.UsuarioExceptionMsg.USER_DATA_NOT_MODIFIED;
import static com.didekinlib.model.usuario.http.UsuarioExceptionMsg.USER_DUPLICATE;
import static com.didekinlib.model.usuario.http.UsuarioExceptionMsg.USER_NOT_FOUND;
import static com.didekinlib.model.usuario.http.UsuarioExceptionMsg.USER_WRONG_INIT;
import static com.didekinlib.model.usuario.http.UsuarioServConstant.IS_USER_DELETED;
import static com.didekinlib.model.usuariocomunidad.Rol.getRolFromFunction;
import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Stream.of;
import static org.mindrot.jbcrypt.BCrypt.checkpw;
import static org.mindrot.jbcrypt.BCrypt.gensalt;
import static org.mindrot.jbcrypt.BCrypt.hashpw;

/**
 * User: pedro@didekin
 * Date: 20/04/15
 * Time: 15:41
 */
public class UsuarioManager {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioManager.class.getCanonicalName());

    private static final int BCRYPT_LOG_ROUNDS = 12;
    static final Supplier<String> BCRYPT_SALT = () -> gensalt(BCRYPT_LOG_ROUNDS);

    final ComunidadDao comunidadDao;
    final UsuarioDao usuarioDao;
    private final UsuarioMailService usuarioMailService;
    final EncrypTkProducerBuilder producerBuilder;
    private final EncrypTkConsumerBuilder consumerBuilder;

    @Autowired
    UsuarioManager(ComunidadDao comunidadDao,
                   UsuarioDao usuarioDao,
                   UsuarioMailService usuarioMailService,
                   EncrypTkProducerBuilder producerBuilderIn,
                   EncrypTkConsumerBuilder consumerBuilderIn)
    {
        this.comunidadDao = comunidadDao;
        this.usuarioDao = usuarioDao;
        this.usuarioMailService = usuarioMailService;
        producerBuilder = producerBuilderIn;
        consumerBuilder = consumerBuilderIn;
    }

    public EncrypTkProducerBuilder getProducerBuilder()
    {
        return producerBuilder;
    }

    //    ============================================================
    //    ................... Methods ................
    /*    ============================================================*/

    public UsuarioComunidad completeWithUserComuRoles(String userName, long comunidadId) throws ServiceException
    {
        logger.debug("completeWithUserComuRoles()");
        return usuarioDao.getUserComuRolesByUserName(userName, comunidadId);
    }

    public boolean deleteUser(String userName) throws ServiceException
    {
        logger.info("deleteUser()");
        return deleteUserAndComunidades(userName) >= 1;
    }

    int deleteUserAndComunidades(String userName) throws ServiceException
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

    public int deleteUserComunidad(final UsuarioComunidad usuarioComunidad) throws ServiceException
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

    Comunidad getComunidadById(long comunidadId) throws ServiceException
    {
        logger.info("getComunidadById()");
        return comunidadDao.getComunidadById(comunidadId);
    }

    public Comunidad getComunidadById(Usuario user, long comunidadId) throws ServiceException
    {
        logger.debug("getComunidadById()");
        if (comunidadDao.existsUserComu(comunidadId, user.getuId())) {
            return comunidadDao.getComunidadById(comunidadId);
        } else {
            throw new ServiceException(USERCOMU_WRONG_INIT);
        }
    }

    public List<Comunidad> getComusByUser(String userName)
    {
        logger.debug("getComusByUser()");
        return usuarioDao.getComusByUser(userName);
    }

    public List<String> getGcmTokensByComunidad(long comunidadId)
    {
        logger.debug("getGcmTokensByComunidad()");
        return of(usuarioDao.getGcmTokensByComunidad(comunidadId))
                .peek(tokensList -> logger.debug("getGcmTokensByComunidad(); gcmTokens size = " + tokensList.size()))
                .findFirst().get();
    }

    List<String> getRolesSecurity(Usuario usuario)
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
     * @return an instance of usuarioComunidad or null, if there exists the comunidad.
     * @throws ServiceException COMUNIDAD_NOT_FOUND, if there is not userComu in DB and there doesn't exist the comunidad.
     */
    public @Null UsuarioComunidad getUserComuByUserAndComu(String userName, long comunidadId) throws ServiceException
    {
        logger.debug("getUserComuFullByUserAndComu()");
        final UsuarioComunidad userComu = usuarioDao.getUserComuFullByUserAndComu(userName, comunidadId);
        if (userComu == null) {
            getComunidadById(comunidadId); // throw COMUNIDAD_NOT_FOUND
        }
        return userComu;
    }


    public @NotNull Usuario getUserData(String email) throws ServiceException
    {
        logger.info("getUserData()");
        return usuarioDao.getUserDataByName(email);
    }

    boolean isOldestUserComu(Usuario user, long comunidadId) throws ServiceException
    {
        logger.debug("isOldestOrAdmonUserComu()");
        long idOldestUser;
        try {
            idOldestUser = usuarioDao.getOldestUserComuId(comunidadId);
        } catch (EmptyResultDataAccessException e) {
            throw new ServiceException(COMUNIDAD_NOT_FOUND);
        }
        return user.getuId() == idOldestUser;
    }

    public boolean isUserInComunidad(String userName, long comunidadId)
    {
        return usuarioDao.isUserInComunidad(userName, comunidadId);
    }

    /**
     * The gcm_token and token_auth are updated in DB.
     *
     * @return a new security token if the token has been inserted in DB.
     * @throws ServiceException if  USER_WRONG_INIT, USER_NOT_FOUND or PASSWORD_WRONG.
     */
    public @NotNull String login(Usuario usuario) throws ServiceException
    {
        logger.debug("login()");

        if (!EMAIL.isPatternOk(usuario.getUserName()) || !PASSWORD.isPatternOk(usuario.getPassword())) {
            throw new ServiceException(USER_WRONG_INIT);
        }

        Usuario newUserToDb = new Usuario.UsuarioBuilder()
                .copyUsuario(getUserData(usuario.getUserName()))
                .gcmToken(usuario.getGcmToken())
                .build();
        if (checkpw(usuario.getPassword(), newUserToDb.getPassword())) {
            return updateUserTokensInDb(newUserToDb);
        }
        throw new ServiceException(PASSWORD_WRONG);
    }

    String makeNewPassword() throws ServiceException
    {
        logger.debug("makeNewPassword()");
        String newPasssword = new PswdGenerator().makePassword();
        if (newPasssword.isEmpty()) {
            throw new ServiceException(PASSWORD_NOT_SENT);
        }
        return newPasssword;
    }

    public int modifyComuData(Usuario user, Comunidad comunidad) throws ServiceException
    {
        logger.info("modifyComuData()");
        if (checkComuDataModificationPower(user, comunidad)) {
            return comunidadDao.modifyComuData(comunidad);
        }
        throw new ServiceException(UNAUTHORIZED_TX_TO_USER);
    }

    /**
     * Preconditions:
     * 1. OldUserName cannot be null.
     * 2. UsuarioId cannot be null.
     * Postconditions:
     * 1. the userName and/or alias have been modified.
     * 2. if userName has been modified, a new password is sent to the user.
     *
     * @return number of rows afected in user table (it should be 1).
     * @throws ServiceException if both newUserName and newAlias are both null.
     */
    public int modifyUser(final Usuario userNew, String oldUserName, String localeToStr) throws ServiceException
    {
        logger.info("modifyUser()");
        Usuario userInDB = usuarioDao.getUserDataById(userNew.getuId());
        boolean isAliasNew = userNew.getAlias() != null && !userNew.getAlias().isEmpty();
        boolean isUserNameNew = userNew.getUserName() != null && !userNew.getUserName().isEmpty() && !userNew.getUserName().equals(oldUserName);

        if (isUserNameNew) {
            Usuario.UsuarioBuilder userToDbBuilder = new Usuario.UsuarioBuilder()
                    .copyUsuario(userInDB)
                    .userName(userNew.getUserName())
                    .password(makeNewPassword());
            Usuario userToDB = isAliasNew ? userToDbBuilder.alias(userNew.getAlias()).build() : userToDbBuilder.build();
            int userModified = usuarioDao.modifyUser(doUserEncryptPswd(userToDB));
            if (userModified > 0) {
                chooseMailService(null).sendMessage(userToDB, localeToStr);
            }
            return userModified;
        } else if (isAliasNew) {
            return usuarioDao.modifyUserAlias(new Usuario.UsuarioBuilder().copyUsuario(userInDB).alias(userNew.getAlias()).build());
        } else {
            throw new ServiceException(USER_DATA_NOT_MODIFIED);
        }
    }

    public int modifyUserComu(UsuarioComunidad userComu)
    {
        logger.info("modifyUserComu()");
        return usuarioDao.modifyUserComu(userComu);
    }

    public int modifyUserGcmTokens(List<GcmTokensHolder> holdersList)
    {
        logger.debug("modifyGcmTokens(List<GcmTokensHolder> holdersList)");
        return (int) holdersList.parallelStream()
                .filter(holder -> holder.getOriginalGcmTk() != null)
                .map(holder -> holder.getNewGcmTk() == null ? usuarioDao.deleteGcmToken(holder.getOriginalGcmTk()) : usuarioDao.modifyUserGcmToken(holder))
                .count();
    }

    /**
     * Preconditions:
     * 1. userName and password must be not null and valid.
     * Postconditions:
     * 1. the password has been modified.
     *
     * @return new authToken.
     * @throws ServiceException if rows affected != 1 or wrong initialization of user data.
     */
    public String passwordChange(final String userName, String oldPassword, final String newPassword) throws ServiceException
    {
        logger.info("passwordChange()");

        if (!EMAIL.isPatternOk(userName)
                || !PASSWORD.isPatternOk(newPassword)
                || !PASSWORD.isPatternOk(oldPassword)) {
            throw new ServiceException(USER_WRONG_INIT);
        }

        Usuario oldUser = usuarioDao.getUserDataByName(userName);
        if (!checkpw(oldPassword, oldUser.getPassword())) {
            throw new ServiceException(PASSWORD_WRONG);
        }

        final Usuario usuarioNew = new Usuario.UsuarioBuilder()
                .copyUsuario(oldUser)
                .password(hashpw(newPassword, gensalt(12)))
                .build();

        if (usuarioDao.passwordChange(usuarioNew) == 1) {
            return updateUserTokensInDb(usuarioNew);
        }
        throw new ServiceException(USER_NOT_FOUND);
    }

    public boolean passwordSend(String userName, String localeToStr, UsuarioMailServiceIf... mailService) throws ServiceException
    {
        logger.debug("passwordSend()");

        final Usuario oldUsuario = getUserData(userName);
        final Usuario usuarioPswdRaw = doUserRawPswd(oldUsuario);
        final Usuario usuarioPswdEncr = doUserEncryptPswd(usuarioPswdRaw);

        try {
            if (usuarioDao.passwordChange(usuarioPswdEncr) == 1) {
                chooseMailService(mailService).sendMessage(usuarioPswdRaw, localeToStr);
                return true;
            } else {
                throw new ServiceException(USER_DATA_NOT_MODIFIED);
            }
        } catch (MailException e) {
            // If password not sent, we restore old encrypted password in BD. She could login with old userName/password.
            usuarioDao.passwordChange(oldUsuario);
            throw new ServiceException(PASSWORD_NOT_SENT);
        }
    }

    public boolean regComuAndUserAndUserComu(final UsuarioComunidad usuarioCom, String localeToStr,
                                             UsuarioMailServiceIf... mailServTest) throws ServiceException
    {
        logger.info("regComuAndUserAndUserComu()");

        // Generate a new password.
        final Usuario usuarioPswdRaw = doUserRawPswd(usuarioCom.getUsuario());
        final UsuarioComunidad userComEncryptPswd =
                new UsuarioComunidad.UserComuBuilder(usuarioCom.getComunidad(), doUserEncryptPswd(usuarioPswdRaw))
                        .userComuRest(usuarioCom).build();

        long pkUsuario = 0;
        long pkComunidad = 0;
        int userComuInserted = 0;
        Connection conn = null;

        try {
            conn = requireNonNull(comunidadDao.getJdbcTemplate().getDataSource()).getConnection();
            conn.setAutoCommit(false);
            pkUsuario = usuarioDao.insertUsuario(userComEncryptPswd.getUsuario(), conn);
            pkComunidad = comunidadDao.insertComunidad(userComEncryptPswd.getComunidad(), conn);

            Usuario userWithPk = new Usuario.UsuarioBuilder().uId(pkUsuario).build();
            Comunidad comuWithPk = new Comunidad.ComunidadBuilder().c_id(pkComunidad).build();
            UsuarioComunidad userComuWithPks = new UsuarioComunidad.UserComuBuilder(comuWithPk, userWithPk)
                    .userComuRest(userComEncryptPswd).build();

            userComuInserted = comunidadDao.insertUsuarioComunidad(userComuWithPks, conn);
            chooseMailService(mailServTest).sendMessage(usuarioPswdRaw, localeToStr);
            conn.commit();
        } catch (SQLException se) {
            doCatchSqlException(conn, se);
        } catch (MailException e) {
            doCatchMailException(conn, e);
        } finally {
            doFinallyJdbc(conn, "regComuAndUserAndUserComu(): ");
        }

        return pkUsuario > 0L && pkComunidad > 0L && userComuInserted == 1;
    }

    public boolean regComuAndUserComu(UsuarioComunidad usuarioCom) throws ServiceException
    {
        logger.info("regComuAndUserComu()");

        Usuario usuario = usuarioCom.getUsuario();
        long pkComunidad = 0;
        int userComuInserted = 0;
        Connection conn = null;

        try {
            conn = requireNonNull(comunidadDao.getJdbcTemplate().getDataSource()).getConnection();
            conn.setAutoCommit(false);
            pkComunidad = comunidadDao.insertComunidad(usuarioCom.getComunidad(), conn);

            Comunidad comuWithPk = new Comunidad.ComunidadBuilder().c_id(pkComunidad).build();
            UsuarioComunidad userComuWithPks = new UsuarioComunidad.UserComuBuilder(comuWithPk, usuario)
                    .userComuRest(usuarioCom).build();
            userComuInserted = comunidadDao.insertUsuarioComunidad(userComuWithPks, conn);
            conn.commit();
        } catch (SQLException se) {
            doCatchSqlException(conn, se);
        } finally {
            doFinallyJdbc(conn, "regComuAndUserComu(): conn.setAutoCommit(true), conn.close(): ");
        }
        return pkComunidad > 0L && userComuInserted == 1;
    }

    public boolean regUserAndUserComu(final UsuarioComunidad userComu, String localeToStr,
                                      UsuarioMailServiceIf... mailServTest) throws ServiceException
    {
        logger.debug("regUserAndUserComu()");

        final Usuario usuarioPswdRaw = doUserRawPswd(userComu.getUsuario());
        final UsuarioComunidad userComEncryptPswd =
                new UsuarioComunidad.UserComuBuilder(userComu.getComunidad(), doUserEncryptPswd(usuarioPswdRaw))
                        .userComuRest(userComu).build();

        long pkUsuario = 0;
        int userComuInserted = 0;
        Connection conn = null;

        try {
            conn = requireNonNull(usuarioDao.getJdbcTemplate().getDataSource()).getConnection();
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            conn.setAutoCommit(false);
            pkUsuario = usuarioDao.insertUsuario(userComEncryptPswd.getUsuario(), conn);
            final Usuario usuarioPk = new Usuario.UsuarioBuilder().uId(pkUsuario).build();
            final UsuarioComunidad userComuTris = new UsuarioComunidad.UserComuBuilder(
                    userComu.getComunidad(), usuarioPk)
                    .userComuRest(userComu)
                    .build();
            userComuInserted = comunidadDao.insertUsuarioComunidad(userComuTris, conn);
            chooseMailService(mailServTest).sendMessage(usuarioPswdRaw, localeToStr);
            conn.commit();
        } catch (SQLException se) {
            doCatchSqlException(conn, se);
        } catch (MailException e) {
            doCatchMailException(conn, e);
        } finally {
            doFinallyJdbc(conn, "regUserAndUserComu(): conn.setAutoCommit(true), conn.close(): ");
        }
        return pkUsuario > 0L && userComuInserted == 1;
    }

    public int regUserComu(UsuarioComunidad usuarioComunidad)
    {
        logger.info("regUserComu()");
        return comunidadDao.insertUsuarioComunidad(usuarioComunidad);
    }

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

    public List<UsuarioComunidad> seeUserComusByComu(String userName, long idComunidad)
    {
        logger.debug("seeUserComusByComu()");
        if (isUserInComunidad(userName, idComunidad)) {
            return usuarioDao.seeUserComusByComu(idComunidad);
        }
        throw new ServiceException(UNAUTHORIZED_TX_TO_USER);
    }

    public List<UsuarioComunidad> seeUserComusByUser(String userName)
    {
        logger.info("seeUserComusByUser()");
        return usuarioDao.seeUserComusByUser(userName);
    }

    /**
     * Authorization tokens are treated as passwords: they are BCrypted before persisted in BD.
     * It updates gcm_token and token_auth in DB.
     *
     * @param usuarioIn contains a new gcm_token.
     * @return null if token is not updated in DB; otherwise it returns the new token_auth.
     */
    String updateUserTokensInDb(Usuario usuarioIn)
    {
        logger.debug("updateUserTokensInDb(usuarioIn)");
        String tokenAuthStr = producerBuilder.defaultHeadersClaims(usuarioIn.getUserName()).build().getEncryptedTkStr();
        return updateUserTokensInDb(usuarioIn, tokenAuthStr);
    }

    String updateUserTokensInDb(Usuario usuarioIn, String newTokenAuthStr)
    {
        logger.debug("updateUserTokensInDb(usuarioIn, newTokenAuthStr)");
        return usuarioDao.updateUserTokensById(usuarioIn, hashpw(newTokenAuthStr, BCRYPT_SALT.get())) ? newTokenAuthStr : null;
    }

    // =================================  CHECKERS ======================================

    /**
     * The method checks if a user is the oldest one in the comunidad or has the authority 'adm'.
     *
     * @param user      : user in session.
     * @param comunidad : comunidad to be modified.
     */
    public boolean checkComuDataModificationPower(Usuario user, Comunidad comunidad) throws ServiceException
    {
        logger.debug("checkIncidModificationPower()");
        return isOldestUserComu(user, comunidad.getC_Id()) || completeWithUserComuRoles(user.getUserName(), comunidad.getC_Id()).hasAdministradorAuthority();
    }


    public String checkHeaderGetUserName(String httpHeaderIn)
    {
        return getUser(httpHeaderIn, Usuario::getUserName);
    }

    /**
     * @throws ServiceException UNAUTHORIZED if the token is different from the one in database or it has an invalid format.
     */
    public Usuario checkHeaderGetUserData(String httpHeaderIn)
    {
        return getUser(httpHeaderIn, identity());
    }

    // =================================  HELPERS ======================================

    static Usuario doUserEncryptPswd(Usuario usuarioPswdRaw)
    {
        // Password encryption.
        return new Usuario.UsuarioBuilder()
                .copyUsuario(usuarioPswdRaw)
                .password(hashpw(usuarioPswdRaw.getPassword(), BCRYPT_SALT.get()))
                .build();
    }

    static void doFinallyJdbc(Connection conn, String msg)
    {
        try {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        } catch (SQLException e) {
            logger.error(msg + e.getMessage());
        }
    }

    static void doCatchSqlException(Connection conn, SQLException se)
    {
        logger.error("doCatchSqlException(): %s%n", se.toString());

        try {
            if (conn != null) {
                conn.rollback();
            }
            if (se.getMessage().contains(DUPLICATE_ENTRY) &&
                    (se.getMessage().contains(USER_NAME) || se.getMessage().contains(GCM_TOKEN_KEY))) {
                throw new ServiceException(USER_DUPLICATE);
            }
            if (se.getMessage().contains(DUPLICATE_ENTRY) && se.getMessage().contains(COMUNIDAD_UNIQUE_KEY)) {
                throw new ServiceException(COMUNIDAD_DUPLICATE);
            }
        } catch (SQLException e) {
            throw new ServiceException(DATABASE_ERROR);
        }
    }

    private Usuario doUserRawPswd(Usuario usuario)
    {
        return new Usuario.UsuarioBuilder()
                .copyUsuario(usuario)
                .password(makeNewPassword())
                .build();
    }

    private UsuarioMailServiceIf chooseMailService(UsuarioMailServiceIf[] mailServiceTest)
    {
        return (mailServiceTest != null && mailServiceTest.length == 1) ? mailServiceTest[0] : usuarioMailService;
    }

    private void doCatchMailException(Connection conn, MailException me)
    {
        logger.error("doCatchMailException(): %s%n", me.getCause().getMessage());

        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException e1) {
                logger.error(me.getMessage());
            }
        }
        throw new ServiceException(PASSWORD_NOT_SENT);
    }

    private Function<AuthHeaderToken, Usuario> getUsuarioFromHeaderFunc()
    {
        return header -> {
            try {
                return getUserData(
                        consumerBuilder.defaultInit(header.getToken()).build().getClaims().getSubject()
                );
            } catch (MalformedClaimException e) {
                throw new ServiceException(UNAUTHORIZED);
            }
        };
    }

    private <T> T getUser(String httpHeaderIn, Function<Usuario, T> mapToUserReturn)
    {
        AuthHeaderToken headerIn = new AuthHeaderToken(httpHeaderIn);
        return of(headerIn)
                .filter(header -> tkEncrypted_direct_symmetricKey_REGEX.isPatternOk(header.getToken()))
                .map(getUsuarioFromHeaderFunc())
                .filter(usuarioDb -> checkpw(headerIn.getToken(), usuarioDb.getTokenAuth()))
                .map(mapToUserReturn)
                .findFirst()
                .orElseThrow(() -> new ServiceException(UNAUTHORIZED));
    }
}