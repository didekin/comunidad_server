package com.didekin.common.repository;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

import static java.lang.System.getenv;

/**
 * w
 * User: pedro@didekin
 * Date: 05/11/15
 * Time: 15:59
 *
 * HikariCP uses milliseconds for all time values.
 */
@Configuration
public class RepositoryConfig {

    private static final String JDBC_URL_DEFAULT_PORT = "3306";
    private static final String SSL_DEFAULT_VALUE = "useSSL=false";
    private static final String DB_NAME = "didekin";

    private static final Logger logger = LoggerFactory.getLogger(RepositoryConfig.class.getCanonicalName());

    /**
     * Note: By default, Liquibase autowires the (@Primary) DataSource in your context and uses that for migrations.
     */
    @Bean
    @Primary
    public DataSource dataSource()
    {
        logger.debug("dataSource()");

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://"
                + getenv("DB_HOST")
                + ":" + JDBC_URL_DEFAULT_PORT
                + "/" + DB_NAME
                + "?" + SSL_DEFAULT_VALUE
        );
        config.setUsername(getenv("DB_USERNAME"));
        config.setPassword(getenv("DB_PASSWORD"));
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.addDataSourceProperty("serverTimezone", "UTC");
        config.addDataSourceProperty("minimumIdle", "1");
        // For optimal throughput this number should be somewhere near (core_count * 2) + effective_spindle_count (Disks)
        config.setMaximumPoolSize(3);
        // Parameters related to mysql --wait_timeout: it should be about 60 second less.
        config.setMaxLifetime(540000);
        // Only applies when minimumIdle is defined to be 'substantially' less than maximumPoolSize.
        config.setMinimumIdle(480000);
        // Performance
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
        // Logging.
//        config.addDataSourceProperty("logSlowQueries", "true");
        config.addDataSourceProperty("dumpQueriesOnException", "true");

        return new HikariDataSource(config);
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource)
    {
        return new JdbcTemplate(dataSource);
    }
}