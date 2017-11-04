package com.didekin.userservice.testutils;


import com.didekin.common.controller.SecurityTestUtils;
import com.didekin.common.testutils.Constant;
import com.didekinlib.http.retrofit.RetrofitHandler;
import com.didekinlib.http.retrofit.UsuarioComunidadEndPoints;
import com.didekinlib.http.retrofit.UsuarioEndPoints;
import com.didekinlib.model.comunidad.Comunidad;
import com.didekinlib.model.comunidad.Municipio;
import com.didekinlib.model.comunidad.Provincia;
import com.didekinlib.model.usuario.Usuario;
import com.didekinlib.model.usuariocomunidad.UsuarioComunidad;

import java.io.IOException;

import static com.didekinlib.model.common.dominio.ValidDataPatterns.PASSWORD;
import static com.didekinlib.model.usuariocomunidad.Rol.ADMINISTRADOR;
import static com.didekinlib.model.usuariocomunidad.Rol.INQUILINO;
import static com.didekinlib.model.usuariocomunidad.Rol.PROPIETARIO;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * User: pedro@didekin
 * Date: 01/09/15
 * Time: 11:32
 */
public final class UsuarioTestUtils {

    private UsuarioTestUtils()
    {
    }

    // ===================================== Datos con IDs =====================================

    public static final Usuario pedro = new Usuario.UsuarioBuilder()
            .uId(3L)
            .alias("pedronevado")
            .password("password3")
            .gcmToken("pedro_gcm_token")
            .userName("pedro@pedro.com").build();

    public static final Usuario luis = new Usuario.UsuarioBuilder()
            .uId(5L)
            .alias("luis_gomez")
            .password("password5")
            .gcmToken("luis_gcm_token")
            .userName("luis@luis.com").build();

    public static final Usuario juan = new Usuario.UsuarioBuilder()
            .uId(7L)
            .alias("juan_no_auth")
            .password("password7")
            .gcmToken("juan_gcm_token")
            .userName("juan@noauth.com").build();

    public static final Usuario pepe = new Usuario.UsuarioBuilder()
            .uId(9L)
            .alias("pepe_no_auth")
            .password("password9")
            .gcmToken("pepe_gcm_token")
            .userName("pepe@noauth.com").build();

    public static final Usuario paco = new Usuario.UsuarioBuilder()
            .uId(11L)
            .alias("paco")
            .password("password11")
            .gcmToken("paco_gcm_token")
            .userName("paco@paco.com").build();

    public static final Comunidad ronda_plazuela_10bis = new Comunidad.ComunidadBuilder()
            .c_id(1L)
            .tipoVia("Ronda")
            .nombreVia("de la Plazuela")
            .numero((short) 10)
            .sufijoNumero("bis")
            .municipio(new Municipio((short) 52, new Provincia((short) 2)))
            .build();

    public static final Comunidad calle_la_fuente_11 = new Comunidad.ComunidadBuilder()
            .c_id(2L)
            .tipoVia("Calle")
            .nombreVia("de la Fuente")
            .numero((short) 11)
            .sufijoNumero("")
            .municipio(new Municipio((short) 66, new Provincia((short) 3)))
            .build();

    public static final Comunidad calle_el_escorial = new Comunidad.ComunidadBuilder()
            .c_id(3L)
            .tipoVia("Calle")
            .nombreVia("de El Escorial")
            .numero((short) 2)
            .municipio(new Municipio((short) 27, new Provincia((short) 4)))
            .build();

    public static final Comunidad calle_plazuela_23 = new Comunidad.ComunidadBuilder()
            .c_id(4L)
            .tipoVia("Calle")
            .nombreVia("de la Plazuela")
            .numero((short) 23)
            .sufijoNumero("")
            .municipio(new Municipio((short) 52, "Motilleja", new Provincia((short) 2, "Albacete")))
            .build();

    public static final Comunidad calle_olmo_55 = new Comunidad.ComunidadBuilder()
            .c_id(6L)
            .tipoVia("Calle")
            .nombreVia("del Olmo")
            .numero((short) 55)
            .sufijoNumero("")
            .municipio(new Municipio((short) 54, new Provincia((short) 2)))
            .build();

    public static final UsuarioComunidad pedro_plazuelas_10bis = makeUsuarioComunidad(
            ronda_plazuela_10bis, pedro, "Centro", null, "3", "J", ADMINISTRADOR.function);

    public static final UsuarioComunidad luis_plazuelas_10bis = makeUsuarioComunidad(
            ronda_plazuela_10bis, luis, null, null, null, null, ADMINISTRADOR.function.concat(",").concat(PROPIETARIO.function));

    public static final UsuarioComunidad pedro_lafuente = makeUsuarioComunidad(
            calle_la_fuente_11, pedro, "A", null, null, null, ADMINISTRADOR.function.concat(",").concat(INQUILINO.function));

    public static final UsuarioComunidad juan_lafuente = makeUsuarioComunidad(
            calle_la_fuente_11, juan, "A", null, null, null, INQUILINO.function);

    public static final UsuarioComunidad pedro_escorial = makeUsuarioComunidad(
            calle_el_escorial, pedro, "A", null, null, null, ADMINISTRADOR.function.concat(",").concat(INQUILINO.function));

    public static final UsuarioComunidad juan_plazuela23 = makeUsuarioComunidad(
            calle_plazuela_23, juan, "JJ", null, null, null, INQUILINO.function);

    public static final UsuarioComunidad paco_plazuela23 = makeUsuarioComunidad(
            calle_plazuela_23, paco, "BC", null, null, null, ADMINISTRADOR.function.concat(",").concat(PROPIETARIO.function));

    public static final UsuarioComunidad paco_olmo = makeUsuarioComunidad(
            calle_olmo_55, paco, "B", null, "Planta 0", "11", PROPIETARIO.function);

    // ===================================== Datos sin IDs =====================================

    public static final Usuario USER_LUIS = new Usuario.UsuarioBuilder()
            .copyUsuario(luis)
            .uId(0L)
            .build();

    public static final Usuario USER_JUAN = new Usuario.UsuarioBuilder()
            .copyUsuario(juan)
            .uId(0L)
            .build();

    public static final Usuario USER_PEPE = new Usuario.UsuarioBuilder()
            .copyUsuario(pepe)
            .uId(0L)
            .build();

    public static final Usuario USER_PACO = new Usuario.UsuarioBuilder()
            .copyUsuario(paco)
            .uId(0L)
            .build();

    // Municipio: Motilleja Provincia: Albacete.
    public static final Comunidad COMU_LA_PLAZUELA_10bis = new Comunidad.ComunidadBuilder()
            .copyComunidadNonNullValues(ronda_plazuela_10bis)
            .c_id(0L)
            .build();

    // Municipio: Elda provincia: Alicante/Alacant
    public static final Comunidad COMU_LA_FUENTE = new Comunidad.ComunidadBuilder()
            .copyComunidadNonNullValues(calle_la_fuente_11)
            .c_id(0L)
            .build();

    public static final Comunidad COMU_LA_PLAZUELA_5 = new Comunidad.ComunidadBuilder()
            .tipoVia("Ronda")
            .nombreVia("de la Plazuela")
            .numero((short) 5)
            .municipio(new Municipio((short) 2, new Provincia((short) 27)))
            .build();


    public static final Comunidad COMU_LA_PLAZUELA_10 = new Comunidad.ComunidadBuilder()
            .tipoVia("Ronda")
            .nombreVia("de la Plazuela")
            .numero((short) 10)
            .municipio(new Municipio((short) 52, new Provincia((short) 2)))
            .build();

    private static final Comunidad COMU_TRAV_PLAZUELA_11 = new Comunidad.ComunidadBuilder()
            .tipoVia("Travesía")
            .nombreVia("de la Plazuela")
            .numero((short) 11)
            .municipio(new Municipio((short) 13, new Provincia((short) 3)))
            .build();

    public static final Comunidad COMU_EL_ESCORIAL = new Comunidad.ComunidadBuilder()
            .copyComunidadNonNullValues(calle_el_escorial)
            .c_id(0L)
            .build();

    public static final Comunidad COMU_REAL = new Comunidad.ComunidadBuilder()
            .tipoVia("Calle")
            .nombreVia("Real")
            .numero((short) 5)
            .sufijoNumero("Bis")
            .municipio(new Municipio((short) 13, new Provincia((short) 3)))
            .build();

    public static final Comunidad COMU_OTRA = new Comunidad.ComunidadBuilder()
            .tipoVia("Calle")
            .nombreVia("Otra")
            .numero((short) 3)
            .municipio(new Municipio((short) 14, new Provincia((short) 45)))
            .build();

    public static final UsuarioComunidad COMU_REAL_JUAN = makeUsuarioComunidad(COMU_REAL, USER_JUAN, "portal", "esc",
            "plantaX", "door12", PROPIETARIO.function);

    public static final UsuarioComunidad COMU_PLAZUELA5_JUAN = makeUsuarioComunidad(COMU_LA_PLAZUELA_5, USER_JUAN, null,
            null, "planta3", "doorA", ADMINISTRADOR.function);

    public static final UsuarioComunidad COMU_REAL_PEPE = makeUsuarioComunidad(COMU_REAL, USER_PEPE, "portal",
            "esc", "plantaY", "door21", PROPIETARIO.function);

    public static final UsuarioComunidad COMU_TRAV_PLAZUELA_PEPE = makeUsuarioComunidad(COMU_TRAV_PLAZUELA_11, USER_PEPE,
            "portalA", null, "planta2", null, INQUILINO.function);

    // ========================================== Métodos ========================================

    public static UsuarioComunidad makeUsuarioComunidad(Comunidad comunidad, Usuario usuario, String portal, String escalera,
                                                        String planta, String puerta, String roles)
    {
        return new UsuarioComunidad.UserComuBuilder(comunidad, usuario)
                .portal(portal)
                .escalera(escalera)
                .planta(planta)
                .puerta(puerta)
                .roles(roles).build();
    }

    public static Usuario insertUsuarioComunidad(UsuarioComunidad usuarioComunidad, UsuarioComunidadEndPoints userComuEndPoint,
                                                 UsuarioEndPoints usuarioEndPoint, RetrofitHandler retrofitHandler) throws IOException
    {
        userComuEndPoint.regComuAndUserAndUserComu(Constant.oneComponent_local_ES, usuarioComunidad).execute();
        return getUserData(usuarioComunidad.getUsuario(), usuarioEndPoint, retrofitHandler);
    }

    public static Usuario getUserData(Usuario usuario, UsuarioEndPoints usuarioEndPoints, RetrofitHandler retrofitHandler) throws IOException
    {
        return usuarioEndPoints.getUserData(new SecurityTestUtils(retrofitHandler).doAuthHeaderFromRemoteToken(usuario.getUserName(),
                usuario.getPassword())).execute().body();
    }

    public static String tokenPedro(RetrofitHandler retrofitHandler) throws IOException
    {
        return new SecurityTestUtils(retrofitHandler).doAuthHeaderFromRemoteToken(pedro.getUserName(), pedro.getPassword());
    }

    public static String tokenLuis(RetrofitHandler retrofitHandler) throws IOException
    {
        return new SecurityTestUtils(retrofitHandler).doAuthHeaderFromRemoteToken(luis.getUserName(), luis.getPassword());
    }

    public static String tokenPepe(RetrofitHandler retrofitHandler) throws IOException
    {
        return new SecurityTestUtils(retrofitHandler).doAuthHeaderFromRemoteToken(USER_PEPE.getUserName(), USER_PEPE.getPassword());
    }

    public static void checkGeneratedPassword(String password)
    {
        assertThat(password.length() <= 13, is(true));
        assertThat(password.length() >= 10, is(true));
        assertThat(password.contains("O"), is(false));
        assertThat(PASSWORD.isPatternOk(password), is(true));
    }
}
