package org.example.realworldapi;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Table;
import jakarta.persistence.metamodel.EntityType;
import jakarta.transaction.Transactional;
import org.hibernate.cfg.Configuration;

import java.util.HashSet;
import java.util.Set;

@ApplicationScoped
public class DatabaseIntegrationTest {

    @Inject
    EntityManager em;

    private Set<String> entities = new HashSet<>();
    private Configuration configuration;

    @PostConstruct
    private void init() {
        this.configuration = new Configuration();
        this.configEntityClasses();
    }

    private void configEntityClasses() {
        // Retrieve all managed entity types from the EntityManager's Metamodel
        Set<EntityType<?>> entityTypes = em.getMetamodel().getEntities();

        for (EntityType<?> entityType : entityTypes) {
            Class<?> entityClass = entityType.getJavaType();

            // Ensure the class is annotated with @Table to retrieve the table name
            Table table = entityClass.getAnnotation(Table.class);
            if (table != null) {
                String tableName = table.name();
                this.entities.add(tableName);
                this.configuration.addAnnotatedClass(entityClass);
            } else {
                // Handle cases where @Table might be missing; optionally use the entity name
                String tableName = entityType.getName();
                this.entities.add(tableName);
                this.configuration.addAnnotatedClass(entityClass);
            }
        }
    }

    @Transactional
    public void truncate() {
        for (String tableName : this.entities) {
            this.em.createNativeQuery("TRUNCATE TABLE " + tableName + " CASCADE;")
              .executeUpdate();
        }
    }
}
