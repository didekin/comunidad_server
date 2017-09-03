package com.didekin.common.repository;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * w
 * User: pedro@didekin
 * Date: 05/11/15
 * Time: 15:59
 */
@Configuration
@PropertySource({"classpath:/application.properties"})
public class RepositoryConfig {

    private static final String JDBC_URL_DEFAULT_PORT = "3306";
    private static final String SSL_DEFAULT_VALUE = "useSSL=false";

    @Bean
    public DataSource dataSource()
    {
        org.apache.tomcat.jdbc.pool.DataSource ds = new org.apache.tomcat.jdbc.pool.DataSource();
        ds.setDriverClassName("com.mysql.jdbc.Driver");
        ds.setInitialSize(5);
        ds.setMaxActive(50);
        ds.setMaxIdle(20);
        ds.setMinIdle(5);
        ds.setTimeBetweenEvictionRunsMillis(35000);
        ds.setTestOnBorrow(true);
        ds.setValidationInterval(35000);
        ds.setRemoveAbandoned(true);
        ds.setRemoveAbandonedTimeout(60);
        ds.setMinEvictableIdleTimeMillis(55000);
        ds.setValidationQuery("/* ping */ SELECT 1");
        // JDBC URL
        String jdbcUrlPort = System.getenv("RDS_LOCAL_PORT") != null ? System.getenv("RDS_LOCAL_PORT") : JDBC_URL_DEFAULT_PORT;
        ds.setUrl("jdbc:mysql://"
                + System.getenv("RDS_HOSTNAME")
                + ":" + jdbcUrlPort
                + "/" + System.getenv("RDS_DB_NAME")
                + "?" + SSL_DEFAULT_VALUE
        );
        ds.setUsername(System.getenv("RDS_USERNAME"));
        ds.setPassword(System.getenv("RDS_PASSWORD"));

        return ds;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource)
    {
        return new JdbcTemplate(dataSource);
    }
}