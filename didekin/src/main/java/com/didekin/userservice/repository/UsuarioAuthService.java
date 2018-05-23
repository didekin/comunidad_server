package com.didekin.userservice.repository;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: pedro@didekin
 * Date: 16/04/15
 * Time: 20:24
 * <p>
 * Dao class used exclusively by Spring Oauth2.
 */
public class UsuarioAuthService /*implements UserDetailsService*/ {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioAuthService.class.getCanonicalName());

    private UsuarioManagerIf sujetosService;

    public UsuarioAuthService(UsuarioManagerIf sujetosService)
    {
        this.sujetosService = sujetosService;
    }   // TODO: descomentar y revisar.

    /*@Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException
    {
        logger.info("loadUserByUsername()");

        Usuario usuario;
        try {
            usuario = sujetosService.getUserByUserName(email);
        } catch (ServiceException e) {
            throw new UsernameNotFoundException(USER_NAME_NOT_FOUND.toString());
        }
        List<String> securityRoles = sujetosService.getRolesSecurity(usuario);

        if (securityRoles.size() > 0) {
            List<GrantedAuthority> authorities = new ArrayList<>();
            for (String rol : securityRoles) {
                authorities.add(new SimpleGrantedAuthority(rol));
            }
            return new User(email, usuario.getPassword(), authorities);
        }
        logger.error("loadUserByUsername(), roles not found");
        throw new UsernameNotFoundException(ROLES_NOT_FOUND.toString());
    }*/
}
