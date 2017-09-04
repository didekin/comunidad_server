package com.didekin.incidservice.testutils;

import com.didekinlib.model.comunidad.Comunidad;
import com.didekinlib.model.comunidad.Municipio;
import com.didekinlib.model.comunidad.Provincia;
import com.didekinlib.model.incidencia.dominio.AmbitoIncidencia;
import com.didekinlib.model.incidencia.dominio.IncidComment;
import com.didekinlib.model.incidencia.dominio.Incidencia;
import com.didekinlib.model.incidencia.dominio.IncidenciaUser;
import com.didekinlib.model.incidencia.dominio.Resolucion;
import com.didekinlib.model.usuario.Usuario;
import com.didekinlib.model.usuariocomunidad.UsuarioComunidad;

import java.sql.Timestamp;
import java.time.Instant;

import static com.didekin.userservice.testutils.UsuarioTestUtils.makeUsuarioComunidad;
import static com.didekinlib.model.usuariocomunidad.Rol.ADMINISTRADOR;
import static com.didekinlib.model.usuariocomunidad.Rol.INQUILINO;
import static com.didekinlib.model.usuariocomunidad.Rol.PROPIETARIO;

/**
 * User: pedro@didekin
 * Date: 20/11/15
 * Time: 11:36
 */
public final class IncidenciaTestUtils {

    private IncidenciaTestUtils()
    {
    }

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

    public static final Comunidad calle_plazuela_23 = new Comunidad.ComunidadBuilder()
            .c_id(4L)
            .tipoVia("Calle")
            .nombreVia("de la Plazuela")
            .numero((short) 23)
            .sufijoNumero("")
            .municipio(new Municipio((short) 52, new Provincia((short) 2)))
            .build();

    public static final Comunidad calle_olmo_55 = new Comunidad.ComunidadBuilder()
            .c_id(6L)
            .tipoVia("Calle")
            .nombreVia("del Olmo")
            .numero((short) 55)
            .sufijoNumero("")
            .municipio(new Municipio((short) 54, new Provincia((short) 2)))
            .build();

    public static final UsuarioComunidad luis_plazuelas_10bis = makeUsuarioComunidad(
            ronda_plazuela_10bis, luis, null, null, null, null, ADMINISTRADOR.function.concat(",").concat(PROPIETARIO.function));

    public static final UsuarioComunidad pedro_lafuente = makeUsuarioComunidad(
            calle_la_fuente_11, pedro, "A", null, null, null, ADMINISTRADOR.function.concat(",").concat(INQUILINO.function));

    public static final UsuarioComunidad luis_lafuente = makeUsuarioComunidad(
            calle_la_fuente_11, luis, "B", null, null, null, PROPIETARIO.function);

    public static final UsuarioComunidad paco_plazuela23 = makeUsuarioComunidad(
            calle_plazuela_23, paco, "BC", null, null, null, ADMINISTRADOR.function.concat(",").concat(PROPIETARIO.function));

    public static final UsuarioComunidad luis_plazuela23 = makeUsuarioComunidad(
            calle_plazuela_23, luis, "2", "escalera 1", "C", null, INQUILINO.function);

    public static final UsuarioComunidad juan_plazuela23 = makeUsuarioComunidad(
            calle_plazuela_23, juan, "3", "escalera 2", "B", null, PROPIETARIO.function);

    public static final UsuarioComunidad paco_olmo = makeUsuarioComunidad(
            calle_olmo_55, paco, "B", null, "Planta 0", "11", PROPIETARIO.function);

    public static Incidencia doIncidencia(String userName, String descripcion, long comunidadId, short ambitoId)
    {
        return new Incidencia.IncidenciaBuilder()
                .comunidad(new Comunidad.ComunidadBuilder().c_id(comunidadId).build())
                .userName(userName)
                .descripcion(descripcion)
                .ambitoIncid(new AmbitoIncidencia(ambitoId))
                .build();
    }

    public static Incidencia doIncidenciaWithId(String userName, long incidenciaId, long comunidadId, short ambitoId)
    {
        return new Incidencia.IncidenciaBuilder()
                .incidenciaId(incidenciaId)
                .comunidad(new Comunidad.ComunidadBuilder().c_id(comunidadId).build())
                .userName(userName)
                .ambitoIncid(new AmbitoIncidencia(ambitoId))
                .build();
    }

    public static Incidencia doIncidenciaWithIdDescUsername(String userName, long incidenciaId, String descripcion, long comunidadId, short ambitoId)
    {
        return new Incidencia.IncidenciaBuilder()
                .incidenciaId(incidenciaId)
                .userName(userName)
                .descripcion(descripcion)
                .comunidad(new Comunidad.ComunidadBuilder().c_id(comunidadId).build())
                .ambitoIncid(new AmbitoIncidencia(ambitoId))
                .build();
    }

    public static IncidenciaUser doIncidenciaUser(Incidencia incidencia, Usuario user)
    {
        return new IncidenciaUser.IncidenciaUserBuilder(incidencia)
                .usuario(new Usuario.UsuarioBuilder()
                        .uId(user.getuId())
                        .userName(user.getUserName())
                        .build())
                .build();
    }

    public static IncidComment doComment(String descComment, Incidencia incidencia, Usuario redactor)
    {
        Incidencia incidenciaIn = new Incidencia.IncidenciaBuilder()
                .incidenciaId(incidencia.getIncidenciaId())
                .comunidad(new Comunidad.ComunidadBuilder().c_id(incidencia.getComunidad().getC_Id()).build())
                .build();

        return new IncidComment.IncidCommentBuilder().descripcion(descComment)
                .incidencia(incidenciaIn)
                .redactor(redactor)
                .build();
    }

    public static Resolucion doResolucion(Incidencia incidencia, String userName, String descripcion, int costeEstimado, Instant fechaPrev)
    {
        return new Resolucion.ResolucionBuilder(incidencia)
                .userName(userName)
                .descripcion(descripcion)
                .costeEstimado(costeEstimado)
                .fechaPrevista(Timestamp.from(fechaPrev))
                .build();
    }
}
