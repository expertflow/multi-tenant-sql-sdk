package io.github.expertflow.config;

import io.github.expertflow.api.TenantRegistry;
import io.github.expertflow.internal.MultiTenantRoutingDataSource;
import io.github.expertflow.internal.TenantExecutionTemplate;
import io.github.expertflow.internal.TenantRegistryImpl;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;

@Configuration
@AutoConfigureAfter(DataSourceAutoConfiguration.class) // ensure default DS exists first
@ConditionalOnProperty(prefix = "multitenant", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MultiTenantAutoConfiguration {

    private final Environment env;

    public MultiTenantAutoConfiguration(Environment env) {
        this.env = env;
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean
    public MultiTenantRoutingDataSource multiTenantRoutingDataSource(
            @Qualifier("defaultDataSource") DataSource defaultDs
    ) {
        return new MultiTenantRoutingDataSource(defaultDs);
    }

    @Bean(name = "entityManagerFactory")
    @ConditionalOnMissingBean(name = "entityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("multiTenantRoutingDataSource") DataSource routingDataSource
    ) {
        String entityPackages = env.getProperty("multitenant.entities.packages");
        if (entityPackages == null || entityPackages.isBlank()) {
            throw new IllegalStateException(
                    "Property 'multitenant.entities.packages' not set! Example: multitenant.entities.packages=com.example.entities"
            );
        }

        return builder
                .dataSource(routingDataSource)
                .packages(entityPackages.split(","))
                .properties(new HashMap<>())
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public org.springframework.orm.jpa.JpaVendorAdapter jpaVendorAdapter() {
        return new org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter();
    }

    @Bean
    @ConditionalOnMissingBean
    public EntityManagerFactoryBuilder entityManagerFactoryBuilder(
            org.springframework.orm.jpa.JpaVendorAdapter jpaVendorAdapter,
            ObjectProvider<org.springframework.orm.jpa.persistenceunit.PersistenceUnitManager> persistenceUnitManager
    ) {
        return new EntityManagerFactoryBuilder(
                jpaVendorAdapter,
                new HashMap<>(),
                persistenceUnitManager.getIfAvailable()
        );
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean
    public org.springframework.orm.jpa.JpaTransactionManager transactionManager(
            jakarta.persistence.EntityManagerFactory emf
    ) {
        return new org.springframework.orm.jpa.JpaTransactionManager(emf);
    }

    @Bean
    @ConditionalOnMissingBean
    public TenantExecutionTemplate tenantExecutionTemplate(
            EntityManagerFactory emf,
            PlatformTransactionManager txManager
    ) {
        return new TenantExecutionTemplate(emf, txManager);
    }

    @Bean
    @ConditionalOnMissingBean
    public TenantRegistry tenantRegistry(MultiTenantRoutingDataSource routingDs) {
        return new TenantRegistryImpl(routingDs);
    }

}