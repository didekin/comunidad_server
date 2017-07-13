package com.didekin.userservice.repository;

import com.didekinlib.model.comunidad.Municipio;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * User: pedro@didekin
 * Date: 28/05/15
 * Time: 18:18
 */
public abstract class MunicipioDaoTest {

    @Autowired
    private MunicipioDao municipioDao;

    /* We assume the tables of comunidad_autonoma, provincia and municipio are populated.*/
    @Test
    public void getMunicipioByPrIdAndMcd() throws Exception
    {
        Municipio municipio = municipioDao.getMunicipioByPrIdAndMcd((short) 13, (short) 3);
        assertThat(municipio.getNombre(), is(equalTo("Alamillo")));

        municipio = municipioDao.getMunicipioById(municipio.getmId());
        assertThat(municipio.getProvincia().getProvinciaId(), is(equalTo((short) 13)));
    }
}