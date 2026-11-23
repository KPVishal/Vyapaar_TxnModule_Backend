package com.bijlipay.config;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitManager;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

@Configuration
public class JpaConfig {
    @Bean
    public EntityManagerFactoryBuilder entityManagerFactoryBuilder(JpaVendorAdapter jpaVendorAdapter, ObjectProvider<PersistenceUnitManager> persistenceUnitManager) {
        Map<String, Object> jpaProps = new HashMap();
        jpaProps.put("hibernate.dialect", "com.bijlipay.util.CustomMySQL5Dialect");
        return new EntityManagerFactoryBuilder(jpaVendorAdapter, jpaProps, (PersistenceUnitManager)persistenceUnitManager.getIfAvailable());
    }

    @Bean
    public JpaVendorAdapter jpaVendorAdapter() {
        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        adapter.setShowSql(false);
        adapter.setGenerateDdl(false);
        return adapter;
    }
}