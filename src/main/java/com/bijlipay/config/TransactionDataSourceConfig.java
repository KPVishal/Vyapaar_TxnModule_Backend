package com.bijlipay.config;

import com.zaxxer.hikari.HikariDataSource;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = {"com.bijlipay.txndb.repository"},
        entityManagerFactoryRef = "transactionEntityManagerFactory",
        transactionManagerRef = "transactionTransactionManager"
)
public class TransactionDataSourceConfig {
    @Value("${transaction.datasource.url}")
    private String url;
    @Value("${transaction.datasource.username}")
    private String username;
    @Value("${transaction.datasource.password}")
    private String password;
    @Value("${transaction.datasource.driver-class-name}")
    private String driverClassName;
    @Value("${transaction.datasource.hikari.maximum-pool-size:20}")
    private int maxPoolSize;
    @Value("${transaction.datasource.hikari.minimum-idle:20}")
    private int minIdle;
    @Value("${transaction.datasource.hikari.connection-timeout:30000}")
    private long connectionTimeout;
    @Value("${transaction.datasource.hikari.idle-timeout:600000}")
    private long idleTimeout;
    @Value("${transaction.datasource.hikari.max-lifetime:1500000}")
    private long maxLifetime;
    @Value("${transaction.datasource.hikari.keepalive-time:300000}")
    private long keepaliveTime;
    @Value("${transaction.datasource.hikari.leak-detection-threshold:10000}")
    private long leakDetectionThreshold;

    @Bean(
            name = {"transactionDataSource"}
    )
    public DataSource transactionDataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(this.url);
        ds.setUsername(this.username);
        ds.setPassword(this.password);
        ds.setDriverClassName(this.driverClassName);
        ds.setPoolName("HikariPool-transaction");
        ds.setMaximumPoolSize(this.maxPoolSize);
        ds.setMinimumIdle(this.minIdle);
        ds.setConnectionTimeout(this.connectionTimeout);
        ds.setIdleTimeout(this.idleTimeout);
        ds.setMaxLifetime(this.maxLifetime);
        ds.setKeepaliveTime(this.keepaliveTime);
        ds.setLeakDetectionThreshold(this.leakDetectionThreshold);
        ds.setConnectionTestQuery("SELECT 1");
        return ds;
    }

    @Bean(
            name = {"transactionEntityManagerFactory"}
    )
    public LocalContainerEntityManagerFactoryBean transactionEntityManagerFactory(EntityManagerFactoryBuilder builder, @Qualifier("transactionDataSource") DataSource dataSource) {
        Map<String, Object> properties = new HashMap();
        properties.put("hibernate.dialect", "com.bijlipay.util.CustomMySQL5Dialect");
        properties.put("hibernate.hbm2ddl.auto", "none");
        properties.put("hibernate.show_sql", "true");
        return builder.dataSource(dataSource).packages(new String[]{"com.bijlipay.txndb.model"}).persistenceUnit("transaction").properties(properties).build();
    }

    @Bean(
            name = {"transactionTransactionManager"}
    )
    public PlatformTransactionManager transactionTransactionManager(@Qualifier("transactionEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}