package com.didekin.incidservice.repository;


import com.didekin.common.EntityException;
import com.didekinlib.model.incidencia.dominio.ImportanciaUser;
import com.didekinlib.model.incidencia.dominio.IncidComment;
import com.didekinlib.model.incidencia.dominio.IncidImportancia;
import com.didekinlib.model.incidencia.dominio.Incidencia;
import com.didekinlib.model.incidencia.dominio.IncidenciaUser;
import com.didekinlib.model.incidencia.dominio.Resolucion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

import static com.didekinlib.model.comunidad.ComunidadExceptionMsg.COMUNIDAD_NOT_FOUND;
import static com.didekinlib.model.incidencia.dominio.IncidenciaExceptionMsg.INCIDENCIA_NOT_FOUND;
import static com.didekinlib.model.incidencia.dominio.IncidenciaExceptionMsg.INCIDENCIA_NOT_REGISTERED;
import static com.didekinlib.model.incidencia.dominio.IncidenciaExceptionMsg.INCID_IMPORTANCIA_NOT_FOUND;
import static com.google.common.base.Preconditions.checkState;

/**
 * User: pedro@didekin
 * Date: 20/04/15
 * Time: 15:41
 */
@Service
class IncidenciaRepoService implements IncidenciaRepoServiceIf {

    private static final Logger logger = LoggerFactory.getLogger(IncidenciaRepoService.class.getCanonicalName());

    private IncidenciaDao incidenciaDao;

    @Autowired
    public IncidenciaRepoService(IncidenciaDao incidenciaDao)
    {
        this.incidenciaDao = incidenciaDao;
    }

    //    ============================================================
    //    .......... IncidenciaRepoServiceIf .......
    //    ============================================================

    @Override
    public void checkIncidenciaOpen(long incidenciaId) throws EntityException
    {
        if (!incidenciaDao.isIncidenciaOpen(incidenciaId)) {
            throw new EntityException(INCIDENCIA_NOT_FOUND);
        }
    }

    @Override
    public int closeIncidencia(long incidenciaId) throws EntityException
    {
        logger.debug("closeIncidencia()");
        int rowsModified = incidenciaDao.closeIncidencia(incidenciaId);
        if (rowsModified == 0) {
            throw new EntityException(INCIDENCIA_NOT_FOUND);
        }
        return rowsModified;
    }

    @Override
    public int deleteIncidencia(long incidenciaId) throws EntityException
    {
        logger.debug("deleteIncidencia()");
        return incidenciaDao.deleteIncidencia(incidenciaId);
    }

    @Override
    public boolean isIncidenciaWithResolucion(long incidenciaId)
    {
        logger.debug("isIncidenciaWithResolucion()");
        return incidenciaDao.countResolucionByIncid(incidenciaId) == 1;
    }


    @Override
    public int modifyIncidencia(Incidencia incidencia) throws EntityException
    {
        logger.debug("modifyIncidencia()");
        int rowsModified = incidenciaDao.modifyIncidencia(incidencia);
        if (rowsModified < 1) {
            checkState(rowsModified == 0);
            throw new EntityException(INCIDENCIA_NOT_FOUND);
        }
        return rowsModified;
    }

    /**
     * If there isn't a incidImportancia record, since we know, once returned from IncidenciaDao that
     * the incidservice is open, we add a new record.
     */
    @Override
    public int modifyIncidImportancia(IncidImportancia incidImportancia) throws EntityException
    {
        logger.debug("modifyIncidImportancia()");

        if (incidenciaDao.modifyIncidImportancia(incidImportancia) < 1) {
            regIncidImportancia(incidImportancia);
        }
        return 1;
    }

    @Override
    public int modifyResolucion(Resolucion resolucion) throws EntityException
    {
        logger.debug("modifyResolucion()");

        int recordsModified = incidenciaDao.modifyResolucion(resolucion);
        if (recordsModified == 1) {
            if (resolucion.getAvances().size() == 1 && !resolucion.getAvances().get(0).getAvanceDesc().trim().isEmpty()) {
                return recordsModified +
                        incidenciaDao.regAvance(resolucion.getIncidencia().getIncidenciaId(), resolucion.getAvances().get(0));
            }
            return recordsModified;
        } else {
            throw new EntityException(INCIDENCIA_NOT_FOUND);
        }
    }

    @Override
    public int regIncidComment(IncidComment comment) throws EntityException
    {
        logger.debug("regIncidComment()");
        if (incidenciaDao.isIncidenciaOpen(comment.getIncidencia().getIncidenciaId())) {
            return incidenciaDao.regIncidComment(comment);
        }
        throw new EntityException(INCIDENCIA_NOT_FOUND);
    }

    @Override
    public long regIncidencia(Incidencia incidencia) throws EntityException
    {
        logger.debug("regIncidencia()");
        try {
            return incidenciaDao.regIncidencia(incidencia);
        } catch (SQLException e) {
            if (e.getMessage().contains(EntityException.COMUNIDAD_FK)) {
                throw new EntityException(COMUNIDAD_NOT_FOUND);
            }
            throw new EntityException(INCIDENCIA_NOT_REGISTERED);
        }
    }

    public int regIncidImportancia(IncidImportancia incidImportancia) throws EntityException
    {
        logger.debug("regIncidImportancia()");
        if (incidenciaDao.isIncidenciaOpen(incidImportancia.getIncidencia().getIncidenciaId())) {
            return incidenciaDao.regIncidImportancia(incidImportancia);
        }
        throw new EntityException(INCIDENCIA_NOT_FOUND);
    }

    @Override
    public int regResolucion(Resolucion resolucion) throws EntityException
    {
        logger.debug("regResolucion()");
        if (incidenciaDao.isIncidenciaOpen(resolucion.getIncidencia().getIncidenciaId())) {
            return incidenciaDao.regResolucion(resolucion);
        }
        throw new EntityException(INCIDENCIA_NOT_FOUND);
    }

    @Override
    public List<IncidComment> seeCommentsByIncid(long incidenciaId) throws EntityException
    {
        logger.debug("seeCommentsByIncid()");
        List<IncidComment> comments = incidenciaDao.SeeCommentsByIncid(incidenciaId);
        if (comments == null || comments.isEmpty()) {
            // Check if the incidservice exists.
            seeIncidenciaById(incidenciaId);
        }
        return comments;
    }

    @Override
    public Incidencia seeIncidenciaById(long incidenciaId) throws EntityException
    {
        logger.debug("seeIncidenciaById()");
        return incidenciaDao.seeIncidenciaById(incidenciaId);
    }

    @Override
    public IncidImportancia seeIncidImportancia(String userName, long incidenciaId) throws EntityException
    {
        logger.debug("seeIncidImportancia()");
        IncidImportancia incidImportancia = null;
        try {
            incidImportancia = incidenciaDao.seeIncidImportanciaByUser(userName, incidenciaId);
        } catch (EntityException e) {
            if (e.getExceptionMsg().equals(INCID_IMPORTANCIA_NOT_FOUND)) {
                return new IncidImportancia.IncidImportanciaBuilder(seeIncidenciaById(incidenciaId)).build();
            }
        }
        return incidImportancia;
    }

    @Override
    public List<IncidenciaUser> seeIncidsClosedByComu(long comunidadId)
    {
        logger.debug("seeIncidsClosedByComu()");
        return incidenciaDao.seeIncidsClosedByComu(comunidadId);
    }

    @Override
    public List<IncidenciaUser> seeIncidsOpenByComu(long comunidadId)
    {
        logger.debug("seeIncidsOpenByComu()");
        return incidenciaDao.seeIncidsOpenByComu(comunidadId);
    }

    @Override
    public Resolucion seeResolucion(long resolucionId) throws EntityException
    {
        logger.debug("seeResolucion()");
        try {
            return incidenciaDao.seeResolucion(resolucionId);
        } catch (EntityException e) {
            return null;
        }
    }

    @Override
    public List<ImportanciaUser> seeUserComusImportancia(long incidenciaId) throws EntityException
    {
        logger.debug("seeUserComusImportancia()");

        List<ImportanciaUser> importanciaUsers = incidenciaDao.seeUserComusImportancia(incidenciaId);
        if (importanciaUsers.size() == 0) {
            throw new EntityException(INCIDENCIA_NOT_FOUND);
        }
        return importanciaUsers;
    }

    // ================================ HELPER METHODS ===================================

}
