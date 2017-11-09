package com.didekin.userservice.repository;

import com.didekin.common.EntityException;
import com.didekin.userservice.mail.UsuarioMailServiceIf;
import com.didekinlib.gcm.model.common.GcmTokensHolder;
import com.didekinlib.model.comunidad.Comunidad;
import com.didekinlib.model.usuario.Usuario;
import com.didekinlib.model.usuariocomunidad.UsuarioComunidad;

import org.springframework.security.oauth2.common.OAuth2AccessToken;

import java.util.List;
import java.util.Optional;

/**
 * User: pedro@didekin
 * Date: 26/08/15
 * Time: 14:37
 */
public interface UsuarioManagerIf {

    Usuario completeUser(String userName) throws EntityException;

    UsuarioComunidad completeWithUserComuRoles(String userName, long comunidadId) throws EntityException;

    boolean deleteAccessToken(String accessTkValue) throws EntityException;

    boolean deleteAccessTokenByUserName(String userName) throws EntityException;

    boolean deleteUser(String userName) throws EntityException;

    int deleteUserAndComunidades(String userName) throws EntityException;

    int deleteUserComunidad(UsuarioComunidad usuarioComunidad) throws EntityException;

    OAuth2AccessToken getAccessToken(String accesTkValue);

    Optional<OAuth2AccessToken> getAccessTokenByUserName(String userName);

    /**
     * Overloaded method, mainly for tests.
     */
    @SuppressWarnings("UnusedReturnValue")
    Comunidad getComunidadById(long comunidadId) throws EntityException;

    Comunidad getComunidadById(Usuario user, long comunidadId) throws EntityException;

    List<Comunidad> getComusByUser(String userName);

    String getGcmToken(long usuarioId);

    List<String> getGcmTokensByComunidad(long comunidadId);

    /**
     * From the functional role, it obtains the security role.
     */
    List<String> getRolesSecurity(Usuario usuario);

    UsuarioComunidad getUserComuByUserAndComu(String userName, long comunidadId) throws EntityException;

    Usuario getUserByUserName(String email) throws EntityException;

    UsuarioDao getUsuarioDao();

    boolean isOldestUserComu(Usuario user, long comunidadId) throws EntityException;

    boolean login(Usuario usuario) throws EntityException;

    String makeNewPassword() throws EntityException;

    int modifyComuData(Usuario user, Comunidad comunidad) throws EntityException;

    int modifyUserGcmToken(Usuario usuario);

    int modifyUserGcmToken(String userName, String gcmToken) throws EntityException;

    int modifyUserGcmTokens(List<GcmTokensHolder> holdersList);

    int modifyUser(Usuario usuario, String oldUserName) throws EntityException;

    int modifyUserComu(UsuarioComunidad userComu);

    int passwordChangeWithName(String userName, String newPassword) throws EntityException;

    boolean passwordSend(String userName, String localeToStr, UsuarioMailServiceIf... usuarioMailService) throws EntityException;

    boolean regComuAndUserAndUserComu(UsuarioComunidad usuarioCom, String localeToStr, UsuarioMailServiceIf... mailServTest) throws EntityException;

    boolean regComuAndUserComu(UsuarioComunidad usuarioCom) throws EntityException;

    boolean regUserAndUserComu(UsuarioComunidad userComu, String localeToStr, UsuarioMailServiceIf... mailServTest) throws EntityException;

    int regUserComu(UsuarioComunidad usuarioComunidad);

    List<Comunidad> searchComunidades(Comunidad comunidad);

    List<UsuarioComunidad> seeUserComusByComu(long idComunidad);

    List<UsuarioComunidad> seeUserComusByUser(String userName);

    boolean checkComuDataModificationPower(Usuario user, Comunidad comunidad) throws EntityException;
}
