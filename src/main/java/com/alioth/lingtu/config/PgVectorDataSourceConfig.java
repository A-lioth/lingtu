package com.alioth.lingtu.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * PostgreSQL 数据源配置（仅用于 PgVector 向量存储）
 */
@Configuration
public class PgVectorDataSourceConfig {

    @Value("${spring.datasource-pgvector.url}")
    private String url;

    @Value("${spring.datasource-pgvector.username}")
    private String username;

    @Value("${spring.datasource-pgvector.password}")
    private String password;

    @Value("${spring.datasource-pgvector.driver-class-name}")
    private String driverClassName;

    /**
     * PostgreSQL 数据源
     */
    @Bean(name = "pgVectorDataSource")
    public DataSource pgVectorDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName(driverClassName);
        // 连接池配置
        dataSource.setMaximumPoolSize(5);
        dataSource.setMinimumIdle(1);
        dataSource.setConnectionTimeout(30000);
        return dataSource;
    }

    /**
     * PostgreSQL JdbcTemplate（用于 PgVector）
     */
    @Bean(name = "pgVectorJdbcTemplate")
    public JdbcTemplate pgVectorJdbcTemplate() {
        return new JdbcTemplate(pgVectorDataSource());
    }
}
