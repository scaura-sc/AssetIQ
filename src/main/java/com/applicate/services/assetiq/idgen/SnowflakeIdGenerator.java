package com.applicate.services.assetiq.idgen;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

/**
 * Hibernate {@link IdentifierGenerator} that delegates to {@link SnowflakeIdWorker}.
 * Requires a public no-arg constructor for Hibernate to instantiate it reflectively
 * (see {@link SnowflakeGenerator}).
 */
public class SnowflakeIdGenerator implements IdentifierGenerator {

    @Override
    public Object generate(SharedSessionContractImplementor session, Object object) {
        return SnowflakeIdWorker.INSTANCE.nextId();
    }
}
