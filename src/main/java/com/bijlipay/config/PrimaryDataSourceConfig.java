package com.bijlipay.config;

import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = {"com.bijlipay.repository"},
        entityManagerFactoryRef = "primaryEntityManagerFactory",
        transactionManagerRef = "primaryTransactionManager"
)
public class PrimaryDataSourceConfig {
    @Primary
    @Bean(
            name = {"primaryDataSourceProperties"}
    )
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties primaryDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Primary
    @Bean(
            name = {"primaryDataSource"}
    )
    public DataSource primaryDataSource() {
        return this.primaryDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Primary
    @Bean(
            name = {"primaryEntityManagerFactory"}
    )
    public LocalContainerEntityManagerFactoryBean primaryEntityManagerFactory(EntityManagerFactoryBuilder builder, @Qualifier("primaryDataSource") DataSource dataSource) {
        Map<String, Object> properties = new HashMap();
        properties.put("hibernate.dialect", "com.bijlipay.util.CustomMySQL5Dialect");
        properties.put("hibernate.hbm2ddl.auto", "none");
        properties.put("hibernate.show_sql", "true");
        return builder.dataSource(dataSource).packages(new String[]{"com.bijlipay.model"}).persistenceUnit("primary").properties(properties).build();
    }

    @Primary
    @Bean(
            name = {"primaryTransactionManager"}
    )
    public PlatformTransactionManager primaryTransactionManager(@Qualifier("primaryEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}