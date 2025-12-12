# multi-tenant-sql-sdk
Plugin to Manage multiple SQL Datasources and Dynamic Selection on the runtime for a multi tenant setup

# 🧩 Multi-Tenant SQL SDK — Developer Guide

*A lightweight, dynamic, tenant-aware JDBC routing library for Spring Boot.*

This SDK enables **dynamic multi-tenant database connections** where each tenant has its own database/schema and DataSource.
It supports **PostgreSQL, MySQL, SQL Server, Oracle, MariaDB**, or any DB supported by JDBC & Spring Boot.

---

# 🚀 1. What This SDK Does

✔ Dynamically registers new tenants at runtime
✔ Routes every DB call to the correct tenant using a ThreadLocal context
✔ Allows the main application to provide its **own DataSource** per tenant
✔ Zero coupling with ORM — works with JPA, MyBatis, Spring JDBC, or raw JDBC
✔ Does NOT require Hibernate multi-tenancy configuration
✔ Optional default datasource for bootstrapping (db creation, metadata access, etc.)

This SDK is meant for **application-level multi-tenancy**, not schema-based automatic creation.

---

# 🏗 2. Installation (Maven)

```xml
<dependency>
    <groupId>io.github.expertflow</groupId>
    <artifactId>multi-tenant-sql-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

---

# ⚙️ 3. Basic Configuration

Add to your `application.properties`:

```properties
multitenant.enabled=true
multitenant.entities.packages=com.example.entities
```

Your app must define **one default datasource** (optional, but recommended):

```java
@Bean(name = "defaultDataSource")
public DataSource defaultDataSource() {
    HikariDataSource ds = new HikariDataSource();
    ds.setJdbcUrl("jdbc:postgresql://localhost:5432/postgres");
    ds.setUsername("postgres");
    ds.setPassword("password");
    ds.setDriverClassName("org.postgresql.Driver");

    ds.setMaximumPoolSize(1);
    return ds;
}
```

---

# 🔑 4. Registering a Tenant Dynamically

Your application is responsible for creating a DataSource.
Example:

```java
@PostMapping("/tenant/register")
public String registerTenant(@RequestBody TenantRequest req) {

    HikariDataSource ds = new HikariDataSource();
    ds.setJdbcUrl(req.getJdbcUrl());
    ds.setUsername(req.getUsername());
    ds.setPassword(req.getPassword());
    ds.setDriverClassName("org.postgresql.Driver");

    tenantRegistry.addDataSource(req.getTenantId(), ds);

    return "Tenant " + req.getTenantId() + " registered";
}
```

---

# 🧭 5. Tenant Context Switching

Every DB operation is performed within a tenant context:

```java
TenantContext.setCurrentTenant(tenantId);
studentRepository.save(entity);
TenantContext.clear();
```

The SDK includes a helper:

### ✔ TenantExecutionTemplate (recommended)

```java
String name = template.execute("tenant1", em -> {
    Student s = new Student("Ali", "ali@test.com");
    em.persist(s);
    return "Saved";
});
```

This:

* switches tenant context
* opens a transactional EntityManager
* runs your lambda
* commits
* closes EM
* clears context

---

# 🧱 6. How Routing Works Internally

```
Your App → TenantContext → MultiTenantRoutingDataSource → Correct DataSource → DB
```

The SDK overrides:

```java
protected Object determineCurrentLookupKey()
```

to pick the correct tenant’s DataSource based on ThreadLocal.

---

# 📦 7. Entity Manager Factory

The SDK auto-configures:

* `EntityManagerFactory`
* `JpaTransactionManager`
* `EntityManagerFactoryBuilder`

and binds them to the **routing datasource**.

You only define your **JPA entities**.

---

# 🧪 8. Example Student Entity

```java
@Entity
@Table(name = "students")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
}
```

---

# ⚡ 9. Usage Examples

### ✔ Insert

```java
template.execute("tenant5", em -> {
    Student s = new Student("Musa", "musa@test.com");
    em.persist(s);
    return null;
});
```

### ✔ Read

```java
List<Student> students = template.execute("tenant5", em ->
        em.createQuery("SELECT s FROM Student s", Student.class).getResultList()
);
```

---

# 🕸 10. Error Handling

If a tenant does not exist:

```
TenantNotFoundException: Tenant 'tenant15' is not registered.
```

If tenant context is missing:

```
NoTenantException: Tenant not set!
```

If Datasource fails to connect, standard SQLExceptions will bubble up.

---

# 📊 11. Performance Expectations

* Routing overhead: **sub-millisecond**
* Works with HikariCP efficiently
* Tenant switch cost: **ThreadLocal lookup only**
* Supports thousands of tenants if pool sizes are controlled

---

# 🛡 12. Best Practices

### ✔ Keep default datasource pool size low

It is only for DB creation or metadata tasks.

### ✔ Keep tenant pools minimal

Most apps use:

```
maxPoolSize = 5–20 per tenant
```

### ✔ Use Unique tenant IDs

To avoid collisions.

### ✔ Call TenantContext.clear()

Only required if you manually set context.

---

# 🤝 13. Contributing

1. Fork the repository
2. Create a feature branch
3. Commit with meaningful messages
4. Submit a Pull Request

---
# 🤝 13. Sample Code
Checkout the following working example
https://github.com/expertflow/SampleMttSqlApp
---

# 📬 14. Support

For issues or suggestions:
👉 [https://github.com/expertflow/multi-tenant-sql-sdk/issues](https://github.com/expertflow/multi-tenant-sql-sdk/issues)

