package com.didekin.userservice.repository;

import com.didekin.common.repository.ServiceException;
import com.didekin.userservice.mail.UsuarioMailServiceIf;
import com.didekinlib.gcm.model.common.GcmTokensHolder;
import com.didekinlib.model.comunidad.Comunidad;
import com.didekinlib.model.usuario.Usuario;
import com.didekinlib.model.usuariocomunidad.UsuarioComunidad;

import java.util.List;

/**
 * User: pedro@didekin
 * Date: 26/08/15
 * Time: 14:37
 */
public interface UsuarioManagerIf {

    Usuario completeUser(String userName) throws ServiceException;

    UsuarioComunidad completeWithUserComuRoles(String userName, long comunidadId) throws ServiceException;

    boolean deleteAccessTokenByUserName(String userName) throws ServiceException;

    boolean deleteUser(String userName) throws ServiceException;

    int deleteUserAndComunidades(String userName) throws ServiceException;

    int deleteUserComunidad(UsuarioComunidad usuarioComunidad) throws ServiceException;

    // TODO: descomentar y revisar.
    /*OAuth2AccessToken getAccessToken(String accesTkValue);

    Optional<OAuth2AccessToken> getAccessTokenByUserName(String userName);*/

    /**
     * Overloaded method, mainly for tests.
     */
    @SuppressWarnings("UnusedReturnValue")
    Comunidad getComunidadById(long comunidadId) throws ServiceException;

    Comunidad getComunidadById(Usuario user, long comunidadId) throws ServiceException;

    List<Comunidad> getComusByUser(String userName);

    String getGcmToken(long usuarioId);

    List<String> getGcmTokensByComunidad(long comunidadId);

    /**
     * From the functional role, it obtains the security role.
     */
    List<String> getRolesSecurity(Usuario usuario);

    UsuarioComunidad getUserComuByUserAndComu(String userName, long comunidadId) throws ServiceException;

    Usuario getUserByUserName(String email) throws ServiceException;

    UsuarioDao getUsuarioDao();

    boolean isOldestUserComu(Usuario user, long comunidadId) throws ServiceException;

    boolean login(Usuario usuario) throws ServiceException;

    String makeNewPassword() throws ServiceException;

    int modifyComuData(Usuario user, Comunidad comunidad) throws ServiceException;

    int modifyUserGcmToken(Usuario usuario);

    int modifyUserGcmToken(String userName, String gcmToken) throws ServiceException;

    int modifyUserGcmTokens(List<GcmTokensHolder> holdersList);

    int modifyUser(Usuario usuario, String oldUserName, String localeToStr) throws ServiceException;

    int modifyUserComu(UsuarioComunidad userComu);

    int passwordChangeWithName(String userName, String newPassword) throws ServiceException;

    boolean passwordSend(String userName, String localeToStr, UsuarioMailServiceIf... usuarioMailService) throws ServiceException;

    boolean regComuAndUserAndUserComu(UsuarioComunidad usuarioCom, String localeToStr, UsuarioMailServiceIf... mailServTest) throws ServiceException;

    boolean regComuAndUserComu(UsuarioComunidad usuarioCom) throws ServiceException;

    boolean regUserAndUserComu(UsuarioComunidad userComu, String localeToStr, UsuarioMailServiceIf... mailServTest) throws ServiceException;

    int regUserComu(UsuarioComunidad usuarioComunidad);

    List<Comunidad> searchComunidades(Comunidad comunidad);

    List<UsuarioComunidad> seeUserComusByComu(long idComunidad);

    List<UsuarioComunidad> seeUserComusByUser(String userName);

    boolean checkComuDataModificationPower(Usuario user, Comunidad comunidad) throws ServiceException;
}
