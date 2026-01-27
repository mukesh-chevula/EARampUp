package com.example.chat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.config.SchemaAction;
import org.springframework.data.cassandra.core.cql.keyspace.CreateKeyspaceSpecification;
import org.springframework.data.cassandra.core.cql.keyspace.KeyspaceOption;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;
import org.springframework.data.cassandra.repository.config.EnableReactiveCassandraRepositories;

import java.util.Collections;
import java.util.List;

@Configuration
@EnableCassandraRepositories(basePackages = "com.example.chat.repository")
public class CassandraConfig extends AbstractCassandraConfiguration {

    @Override
    protected String getKeyspaceName() {
        return "chat_db";
    }

    @Override
    protected String getContactPoints() {
        return "localhost";
    }

    @Override
    protected int getPort() {
        return 9042;
    }

    @Override
    public SchemaAction getSchemaAction() {
        return SchemaAction.CREATE_IF_NOT_EXISTS;
    }

    @Override
    protected List<CreateKeyspaceSpecification> getKeyspaceCreations() {
        return Collections.singletonList(
            CreateKeyspaceSpecification.createKeyspace(getKeyspaceName())
                .ifNotExists()
                .with(KeyspaceOption.DURABLE_WRITES, true)
                .withSimpleReplication(1L)
        );
    }

    @Override
    protected String getLocalDataCenter() {
        return "datacenter1";
    }

    @Override
    public String[] getEntityBasePackages() {
        return new String[]{"com.example.chat.entity"};
    }
}
