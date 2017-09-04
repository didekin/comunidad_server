package com.didekin.incidservice.repository;

import com.didekin.common.EntityException;
import com.didekinlib.model.incidencia.dominio.ImportanciaUser;
import com.didekinlib.model.incidencia.dominio.IncidAndResolBundle;
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
public interface IncidenciaManagerIf {

    int closeIncidencia(String userName, Resolucion resolucion) throws EntityException;

    int deleteIncidencia(String userNameInSession, long incidenciaId) throws EntityException;

    int modifyIncidencia(String userNameInSession, Incidencia incidencia) throws EntityException;

    int modifyIncidImportancia(String userNameFromAuthentication, IncidImportancia incidImportancia) throws EntityException;

    int regIncidImportancia(String userNameFromAuthentication, IncidImportancia incidImportancia) throws EntityException;

    Incidencia regIncidencia(Incidencia incidencia) throws EntityException;

    int regIncidComment(String userNameFromAuthentication, IncidComment build) throws EntityException;

    int regResolucion(String userNameFromAuthentication, Resolucion resolucion) throws EntityException;

    List<IncidComment> seeCommentsByIncid(long incidenciaId) throws EntityException;

    Incidencia seeIncidenciaById(long incidenciaId) throws EntityException;

    IncidAndResolBundle seeIncidImportanciaByUser(String userName, long incidenciaId) throws EntityException;

    List<IncidenciaUser> seeIncidsOpenByComu(String userNameFromAuthentication, long comunidadId);

    Resolucion seeResolucion(String userNameFromAuthentication, long resolucionId) throws EntityException;

    int modifyResolucion(String userName, Resolucion resolucion) throws EntityException;

    List<IncidenciaUser> seeIncidsClosedByComu(String userNameFromAuthentication, long comunidadId);

    List<ImportanciaUser> seeUserComusImportancia(String userNameFromAuthentication, long incidenciaId) throws EntityException;

    UserManagerConnector getUsuarioConnector();

    boolean checkIncidenciaOpen(long incidenciaId);

    boolean checkIncidImportanciaInDb(String userNameInSession, long incidenciaId);
}
