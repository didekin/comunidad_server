package com.didekin.incidservice.repository;

import com.didekin.common.repository.ServiceException;
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

    int closeIncidencia(String userName, Resolucion resolucion) throws ServiceException;

    int deleteIncidencia(String userNameInSession, long incidenciaId) throws ServiceException;

    int modifyIncidImportancia(String userNameFromAuthentication, IncidImportancia incidImportancia) throws ServiceException;

    int regIncidImportancia(String userNameFromAuthentication, IncidImportancia incidImportancia) throws ServiceException;

    int regIncidComment(String userNameFromAuthentication, IncidComment build) throws ServiceException;

    int regResolucion(String userNameFromAuthentication, Resolucion resolucion) throws ServiceException;

    List<IncidComment> seeCommentsByIncid(long incidenciaId) throws ServiceException;

    Incidencia seeIncidenciaById(long incidenciaId) throws ServiceException;

    IncidAndResolBundle seeIncidImportanciaByUser(String userName, long incidenciaId) throws ServiceException;

    List<IncidenciaUser> seeIncidsOpenByComu(String userNameFromAuthentication, long comunidadId);

    Resolucion seeResolucion(String userNameFromAuthentication, long incidenciaId) throws ServiceException;

    int modifyResolucion(String userName, Resolucion resolucion) throws ServiceException;

    List<IncidenciaUser> seeIncidsClosedByComu(String userNameFromAuthentication, long comunidadId);

    List<ImportanciaUser> seeUserComusImportancia(String userNameFromAuthentication, long incidenciaId) throws ServiceException;

    UserManagerConnector getUsuarioConnector();

    boolean checkIncidenciaOpen(long incidenciaId);

    boolean checkIncidImportanciaInDb(String userNameInSession, long incidenciaId);
}
