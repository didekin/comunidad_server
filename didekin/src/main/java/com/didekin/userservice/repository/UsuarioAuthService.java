package com.didekin.userservice.repository;


import com.didekin.common.EntityException;
import com.didekinlib.model.usuario.Usuario;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

import static com.didekinlib.model.usuario.UsuarioExceptionMsg.USER_NAME_NOT_FOUND;
import static com.didekinlib.model.usuariocomunidad.UsuarioComunidadExceptionMsg.ROLES_NOT_FOUND;

/**
 * User: pedro@didekin
 * Date: 16/04/15
 * Time: 20:24
 * <p>
 * Dao class used exclusively by Spring Oauth2.
 */
public class UsuarioAuthService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioAuthService.class.getCanonicalName());

    private UsuarioServiceIf sujetosService;

    public UsuarioAuthService(UsuarioServiceIf sujetosService)
    {
        this.sujetosService = sujetosService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException
    {
        logger.info("loadUserByUsername()");

        Usuario usuario;
        try {
            usuario = sujetosService.getUserByUserName(email);
        } catch (EntityException e) {
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
    }
}
