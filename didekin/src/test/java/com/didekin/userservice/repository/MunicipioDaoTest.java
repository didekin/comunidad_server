package com.didekin.userservice.repository;

import com.didekin.common.DbPre;
import com.didekin.common.LocalDev;
import com.didekinlib.model.comunidad.Municipio;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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

    @Test
    public void getMunicipioCharacterSet() throws Exception
    {
        Municipio municipio = municipioDao.getMunicipioByPrIdAndMcd((short) 13, (short) 1);
        assertThat(municipio.getNombre(), is(equalTo("Abenójar")));
        municipio = municipioDao.getMunicipioByPrIdAndMcd((short) 13, (short) 61);
        assertThat(municipio.getNombre(), is(equalTo("Pedro Muñoz")));
    }

    // ======================================  INNER CLASSES ======================================

    /**
     * User: pedro@didekin
     * Date: 19/04/15
     * Time: 11:18
     */
    @RunWith(SpringJUnit4ClassRunner.class)
    @ContextConfiguration(classes = {UsuarioRepoConfiguration.class})
    @Category({LocalDev.class})
    public static class MunicipioDaoDevTest extends MunicipioDaoTest {
    }

    /**
     * User: pedro@didekin
     * Date: 19/04/15
     * Time: 11:18
     */
    @RunWith(SpringJUnit4ClassRunner.class)
    @ContextConfiguration(classes = {UsuarioRepoConfiguration.class})
    @Category({DbPre.class})
    public static class MunicipioDaoPreTest extends MunicipioDaoTest {
    }
}