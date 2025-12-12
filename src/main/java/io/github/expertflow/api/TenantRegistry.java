package io.github.expertflow.api;

import javax.sql.DataSource;

/**
 * Public API exposed to client applications.
 * The client only needs to register tenant data sources via addDataSource.
 */
public interface TenantRegistry {
    void addDataSource(String tenantId, DataSource dataSource);
    void removeDataSource(String tenantId);
}