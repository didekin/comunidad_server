package com.didekin.userservice.mail;

import com.didekinlib.model.usuario.Usuario;

/**
 * User: pedro@didekin
 * Date: 09/11/2017
 * Time: 14:05
 */
public interface UsuarioMailServiceIf {
    void sendMessage(Usuario user, String localeToStr);
}
