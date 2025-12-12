package io.github.expertflow.internal;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Internal routing DataSource. Client uses TenantRegistry.addDataSource to register a tenant.
 * Library updates target datasource map dynamically and refreshes the routing datasource.
 */
public class MultiTenantRoutingDataSource extends AbstractRoutingDataSource {


    private final Map<Object, Object> targetMap = new ConcurrentHashMap<>();


    public MultiTenantRoutingDataSource(DataSource defaultDataSource) {
        targetMap.put("__default__", defaultDataSource);
        super.setDefaultTargetDataSource(defaultDataSource);
        super.setTargetDataSources(targetMap);
        super.afterPropertiesSet();
    }


    public void addTenant(String tenantId, DataSource ds) {
        targetMap.put(tenantId, ds);
// update parent target map and reinitialise
        super.setTargetDataSources(targetMap);
        super.afterPropertiesSet();
    }


    @Override
    protected Object determineCurrentLookupKey() {
        String t = TenantContext.getCurrentTenant();
        if (t == null) {
            return "__de00fault__";
        }
        return t;
    }

    public void removeTenant(String tenantId) {
        targetMap.remove(tenantId);
        super.setTargetDataSources(targetMap);
        super.afterPropertiesSet();
    }
}