package com.applicate.services.assetiq.support;

import org.junit.jupiter.api.AfterAll;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Base class for repository / migration integration tests (name them
 * {@code *IT.java} so failsafe picks them up).
 *
 * <p>Which engine gets a container depends on the {@code spring.profiles.active}
 * system property, set per Maven profile in {@code pom.xml}:
 *
 * <pre>
 *   mvn verify              -> mysql profile (default) -> MySQL 8 container
 *   mvn verify -Ppostgres   -> Postgres 15 container
 * </pre>
 *
 * <p>CI runs both as separate matrix legs so every IT class here executes
 * against both engines — a single local run only exercises one. This trades
 * "one command covers both engines" for a fast, ordinary Spring context and
 * plain {@code @Test} methods (no forced context rebuilds, no
 * {@code @TestTemplate}).
 *
 * <p>Concrete IT classes just extend this and write normal {@code @Test}
 * methods against whatever repository they're covering — the datasource,
 * dialect, and Liquibase changelog are already wired via the active Spring
 * profile.
 *
 * <p>webEnvironment=MOCK (rather than NONE) + {@code @AutoConfigureMockMvc} so
 * controller-level IT classes can drive the real REST layer (TenantFilter,
 * controllers, GlobalExceptionHandler) via {@code MockMvc} without binding an
 * actual port; this is backward compatible with the plain repository ITs,
 * which just ignore the MockMvc bean.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public abstract class AbstractIntegrationTest {

    private static final String ENGINE = System.getProperty("spring.profiles.active", "mysql");

    private static MySQLContainer<?> mysql;
    private static PostgreSQLContainer<?> postgres;

    @DynamicPropertySource
    static void dataSourceProperties(DynamicPropertyRegistry registry) {
        if ("postgres".equalsIgnoreCase(ENGINE)) {
            postgres = new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("assetiq")
                    .withUsername("assetiq")
                    .withPassword("assetiq");
            postgres.start();

            registry.add("spring.datasource.url", postgres::getJdbcUrl);
            registry.add("spring.datasource.username", postgres::getUsername);
            registry.add("spring.datasource.password", postgres::getPassword);
            registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
            registry.add("spring.liquibase.driver-class-name", postgres::getDriverClassName);
        } else {
            mysql = new MySQLContainer<>("mysql:8.0")
                    .withDatabaseName("assetiq")
                    .withUsername("assetiq")
                    .withPassword("assetiq");
            mysql.start();

            registry.add("spring.datasource.url", mysql::getJdbcUrl);
            registry.add("spring.datasource.username", mysql::getUsername);
            registry.add("spring.datasource.password", mysql::getPassword);
            registry.add("spring.datasource.driver-class-name", mysql::getDriverClassName);
            registry.add("spring.liquibase.driver-class-name", mysql::getDriverClassName);
        }
    }

    @AfterAll
    static void stopContainers() {
        if (mysql != null) {
            mysql.stop();
        }
        if (postgres != null) {
            postgres.stop();
        }
    }
}
