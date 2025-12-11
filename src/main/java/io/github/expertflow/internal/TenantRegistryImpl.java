package io.github.expertflow.internal;

import io.github.expertflow.api.TenantRegistry;
import org.springframework.stereotype.Component;


import javax.sql.DataSource;


/**
 * Implementation of the public TenantRegistry.
 * It registers datasources with the internal routing datasource.
 */
@Component
public class TenantRegistryImpl implements TenantRegistry {


    private final MultiTenantRoutingDataSource routingDataSource;


    public TenantRegistryImpl(MultiTenantRoutingDataSource routingDataSource) {
        this.routingDataSource = routingDataSource;
    }


    @Override
    public void addDataSource(String tenantId, DataSource dataSource) {
        routingDataSource.addTenant(tenantId, dataSource);
    }
}