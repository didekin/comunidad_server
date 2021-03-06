package com.didekin.userservice.testutils;


import com.didekin.common.repository.ServiceException;
import com.didekin.userservice.auth.EncrypTkProducerBuilder;
import com.didekin.userservice.auth.TkParamNames;
import com.didekin.userservice.repository.PswdGenerator.AsciiInterval;
import com.didekin.userservice.repository.UsuarioManager;
import com.didekinlib.model.comunidad.Comunidad;
import com.didekinlib.model.comunidad.Municipio;
import com.didekinlib.model.comunidad.Provincia;
import com.didekinlib.model.usuario.Usuario;
import com.didekinlib.model.usuariocomunidad.UsuarioComunidad;

import java.util.HashMap;
import java.util.Map;

import static com.didekin.userservice.auth.TkParamNames.subject;
import static com.didekin.userservice.repository.PswdGenerator.AsciiInterval.values;
import static com.didekinlib.model.common.dominio.ValidDataPatterns.PASSWORD;
import static com.didekinlib.model.usuario.http.UsuarioExceptionMsg.USER_NOT_FOUND;
import static com.didekinlib.model.usuariocomunidad.Rol.ADMINISTRADOR;
import static com.didekinlib.model.usuariocomunidad.Rol.INQUILINO;
import static com.didekinlib.model.usuariocomunidad.Rol.PROPIETARIO;
import static java.nio.charset.StandardCharsets.US_ASCII;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

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
            .userName("pedro@didekin.es").build();

    public static final Usuario luis = new Usuario.UsuarioBuilder()
            .uId(5L)
            .alias("luis_gomez")
            .password("password5")
            .gcmToken("luis_gcm_token")
            .userName("luis@luis.com")
            .tokenAuth("luis_token_auth").build();

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

    public static final String gcm_token_1 =
            "cuOPgcbcLOk:APA91bG5SNHM79UP_4-Du1p3aRL1aphofK5GcTKjNslh3Zgm1fPH2yI031w1BV6ukVfCQtfdpAk-OlKo2hS6_zY0fEx9ZWpDWNPMWmCIRPK-XMOpysWFlswa0k3ou8_78zvl5doGUam9";

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

    // ============================  Authorization ====================================

    public static Map<TkParamNames, Object> getDefaultTestClaims(Usuario usuario)
    {
        Map<TkParamNames, Object> claimsIn = new HashMap<>(2);
        claimsIn.putIfAbsent(subject, usuario.getUserName());
        return claimsIn;
    }

    public static String doHttpAuthHeader(Usuario usuario, EncrypTkProducerBuilder producerBuilder)
    {
        return producerBuilder.defaultHeadersClaims(usuario.getUserName()).build().getEncryptedTkStr();
    }

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

    // ============================  Checks ====================================

    public static void checkGeneratedPassword(String password, int passwordLength)
    {
        assertThat(password.length() == passwordLength, is(true));
        byte[] pswdBytes = password.getBytes(US_ASCII);
        int pswdInt;
        for (Byte pswdByte : pswdBytes) {
            pswdInt = pswdByte;
            boolean isInside = false;
            for (AsciiInterval interval : values()) {
                if (interval.isInside(pswdInt)) {
                    isInside = true;
                    break;
                }
            }
            assertThat(isInside, is(true));
            assertThat(pswdInt, allOf(not(is(73)), not(is(108))));
        }
        assertThat(PASSWORD.isPatternOk(password), is(true));
    }

    public static void checkUserNotFound(String userName, UsuarioManager usuarioManager)
    {
        try {
            usuarioManager.getUserData(userName);
            fail();
        } catch (ServiceException e) {
            assertThat(e.getExceptionMsg(), is(USER_NOT_FOUND));
        }
    }

    public static void checkBeanUsuario(Usuario usuario, Usuario expectedUser, boolean isHashed)
    {
        assertThat(usuario, allOf(
                hasProperty("uId", anyOf(
                        equalTo(expectedUser.getuId()),
                        greaterThan(0L)
                )),
                hasProperty("userName", equalTo(expectedUser.getUserName())),
                hasProperty("alias", equalTo(expectedUser.getAlias())),
                hasProperty("gcmToken", equalTo(expectedUser.getGcmToken())),
                hasProperty("tokenAuth", equalTo(expectedUser.getTokenAuth()))
        ));
        if (!isHashed) {
            assertThat(expectedUser.getPassword(), is(usuario.getPassword()));
        }
    }
}
