package com.didekin.incidservice.testutils;

import com.didekinlib.model.comunidad.Comunidad;
import com.didekinlib.model.incidencia.dominio.AmbitoIncidencia;
import com.didekinlib.model.incidencia.dominio.IncidComment;
import com.didekinlib.model.incidencia.dominio.Incidencia;
import com.didekinlib.model.incidencia.dominio.IncidenciaUser;
import com.didekinlib.model.incidencia.dominio.Resolucion;
import com.didekinlib.model.usuario.Usuario;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * User: pedro@didekin
 * Date: 20/11/15
 * Time: 11:36
 */
public final class IncidenciaTestUtils {

    private IncidenciaTestUtils()
    {
    }

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
