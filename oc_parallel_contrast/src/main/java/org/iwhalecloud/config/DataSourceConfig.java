package org.iwhalecloud.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
@PropertySource(value = "classpath:/META-INF/dataSource.properties")
public class DataSourceConfig {

    @Bean("bpJdbcTemplate")
    public JdbcTemplate mysqlJdbcTemplate(@Qualifier("bpDataSource")DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
    @Bean("fkJdbcTemplate")
    public JdbcTemplate oracleJdbcTemplate(@Qualifier("fkDataSource")DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
