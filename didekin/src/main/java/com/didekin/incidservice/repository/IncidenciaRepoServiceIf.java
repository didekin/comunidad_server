package com.didekin.incidservice.repository;

import com.didekin.common.EntityException;
import com.didekinlib.model.incidencia.dominio.ImportanciaUser;
import com.didekinlib.model.incidencia.dominio.IncidComment;
import com.didekinlib.model.incidencia.dominio.IncidImportancia;
import com.didekinlib.model.incidencia.dominio.Incidencia;
import com.didekinlib.model.incidencia.dominio.IncidenciaUser;
import com.didekinlib.model.incidencia.dominio.Resolucion;

import java.util.List;

/**
 * User: pedro@didekin
 * Date: 19/11/15
 * Time: 14:01
 */
public interface IncidenciaRepoServiceIf {

    void checkIncidenciaOpen(long incidenciaId) throws EntityException;

    int closeIncidencia(long incidenciaId) throws EntityException;

    int deleteIncidencia(long incidenciaId) throws EntityException;

    boolean isIncidenciaWithResolucion(long incidenciaId);

    int modifyIncidencia(Incidencia incidencia) throws EntityException;

    int modifyIncidImportancia(IncidImportancia incidImportancia) throws EntityException;

    int regIncidImportancia(IncidImportancia incidImportancia) throws EntityException;

    long regIncidencia(Incidencia incidencia) throws EntityException;

    int regIncidComment(IncidComment build) throws EntityException;

    int regResolucion(Resolucion resolucion) throws EntityException;

    List<IncidComment> seeCommentsByIncid(long incidenciaId) throws EntityException;

    Incidencia seeIncidenciaById(long incidenciaId) throws EntityException;

    IncidImportancia seeIncidImportancia(String userName, long incidenciaId) throws EntityException;

    List<IncidenciaUser> seeIncidsOpenByComu(long comunidadId);

    Resolucion seeResolucion(long resolucionId) throws EntityException;

    int modifyResolucion(Resolucion resolucion) throws EntityException;

    List<IncidenciaUser> seeIncidsClosedByComu(long comunidadId);

    List<ImportanciaUser> seeUserComusImportancia(long incidenciaId) throws EntityException;
}
