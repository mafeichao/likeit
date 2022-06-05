package com.likeit.search.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

/**
 * @author mafeichao
 */
@Slf4j
@Configuration
@MapperScan(basePackages = {"com.likeit.search.dao.likeit"}, sqlSessionFactoryRef = ConfigLikeitDb.LIKEIT_FACTORY)
public class ConfigLikeitDb {
    public static final String LIKEIT_FACTORY = "likeitSF";
    public static final String LIKEIT_DATASOURCE = "dataSource";

    @ConfigurationProperties(prefix = "spring.datasource.likeit")
    @Bean(name = LIKEIT_DATASOURCE)
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public DataSourceTransactionManager dataSourceTransactionManager(@Qualifier(LIKEIT_DATASOURCE) DataSource dataSource){
        DataSourceTransactionManager dataSourceTransactionManager=new DataSourceTransactionManager();
        dataSourceTransactionManager.setDataSource(dataSource);
        return dataSourceTransactionManager;
    }

    @Bean(name = LIKEIT_FACTORY)
    @Primary
    public SqlSessionFactory sqlSessionFactory(@Qualifier(LIKEIT_DATASOURCE) DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
        configuration.setMapUnderscoreToCamelCase(true);
        bean.setConfiguration(configuration);
        bean.setDataSource(dataSource);
        return bean.getObject();
    }
}
