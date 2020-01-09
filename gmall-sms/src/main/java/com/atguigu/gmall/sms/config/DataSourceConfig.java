package com.atguigu.gmall.sms.config;

import com.zaxxer.hikari.HikariDataSource;
import io.seata.rm.datasource.DataSourceProxy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * 数据源配置
 *
 * @author HelloWoodes
 */
@Configuration
public class DataSourceConfig {

  /*  @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public HikariDataSource druidDataSource(@Value("${spring.datasource.url}") String url) {

        return hikariDataSource;
    }*/

    /**
     * 需要将 DataSourceProxy 设置为主数据源，否则事务无法回滚
     *
     * @param hikariDataSource The DruidDataSource
     * @return The default datasource
     */
    @Primary
    @Bean("dataSource")
    public DataSource dataSource(@Value("${spring.datasource.url}") String url,@Value("${spring.datasource.password}")String password
    ,@Value("${spring.datasource.driver-class-name}") String driverClassName,@Value("${spring.datasource.username}") String username) {
        HikariDataSource hikariDataSource=new HikariDataSource();
        hikariDataSource.setJdbcUrl(url);
        hikariDataSource.setUsername(username);
        hikariDataSource.setDriverClassName(driverClassName);
        hikariDataSource.setPassword(password);
        return new DataSourceProxy(hikariDataSource);
    }
}
