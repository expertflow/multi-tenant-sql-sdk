package io.github.expertflow.internal;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Function;

/**
 * Internal utility that runs code in a tenant context and within a transaction.
 * This is used by library-managed repository wiring; kept internal so client does not need to use it directly.
 */
public class TenantExecutionTemplate {

    private final EntityManagerFactory emf;
    private final PlatformTransactionManager txManager;

    public TenantExecutionTemplate(EntityManagerFactory emf, PlatformTransactionManager txManager) {
        this.emf = emf;
        this.txManager = txManager;
    }

    public <T> T execute(String tenantId, Function<EntityManager, T> work) {
        try {
            TenantContext.setCurrentTenant(tenantId);
            EntityManager em = emf.createEntityManager();
            TransactionTemplate tt = new TransactionTemplate(txManager);
            try {
                return tt.execute(status -> work.apply(em));
            } finally {
                if (em.isOpen()) em.close();
            }
        } finally {
            TenantContext.clear();
        }
    }
}
