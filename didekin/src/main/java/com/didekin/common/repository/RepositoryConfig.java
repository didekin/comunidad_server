package com.didekin.common.repository;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

import static java.lang.System.getenv;

/**
 * w
 * User: pedro@didekin
 * Date: 05/11/15
 * Time: 15:59
 */
@Configuration
public class RepositoryConfig {

    private static final String JDBC_URL_DEFAULT_PORT = "3306";
    private static final String SSL_DEFAULT_VALUE = "useSSL=false";
    private static final String DB_NAME = "didekin";
    private static final String jdbcUrlPort = getenv("RDS_LOCAL_PORT") != null ? getenv("RDS_LOCAL_PORT") : JDBC_URL_DEFAULT_PORT;

    @Bean
    public DataSource dataSource()
    {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://"
                + getenv("RDS_HOSTNAME")
                + ":" + jdbcUrlPort
                + "/" + DB_NAME
                + "?" + SSL_DEFAULT_VALUE
        );
        config.setUsername(getenv("RDS_USERNAME"));
        config.setPassword(getenv("RDS_PASSWORD"));
        config.setDriverClassName("com.mysql.jdbc.Driver");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "150");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "1024");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("useLocalTransactionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");

        return new HikariDataSource(config);
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource)
    {
        return new JdbcTemplate(dataSource);
    }
}