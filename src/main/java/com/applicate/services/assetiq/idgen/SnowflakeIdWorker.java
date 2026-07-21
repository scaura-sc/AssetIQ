package com.applicate.services.assetiq.idgen;

/**
 * Twitter-Snowflake-style 64-bit ID generator.
 *
 * <p>Layout (MSB to LSB): 1 unused sign bit + 41-bit timestamp (ms since a
 * custom epoch) + 10-bit worker id + 12-bit per-millisecond sequence.
 *
 * <p>This exists so that primary keys are generated identically by the
 * application on both MySQL and Postgres — see the design rule against
 * AUTO_INCREMENT / SERIAL / IDENTITY columns, whose behavior (gaps, batch
 * allocation, restart semantics) differs across the two engines.
 *
 * <p><b>Multi-instance deployments must set a unique worker id per running
 * instance</b> via the {@code SNOWFLAKE_WORKER_ID} system property or
 * environment variable (0-1023), otherwise two instances on the same
 * millisecond can collide. Defaults to 0, which is fine for local/single-instance
 * dev only.
 */
public final class SnowflakeIdWorker {

    public static final SnowflakeIdWorker INSTANCE = new SnowflakeIdWorker(resolveWorkerId());

    /** 2025-01-01T00:00:00Z — arbitrary custom epoch to keep the timestamp component small. */
    private static final long EPOCH = 1735689600000L;

    private static final long WORKER_ID_BITS = 10L;
    private static final long SEQUENCE_BITS = 12L;
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS); // 1023
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);  // 4095
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;

    private final long workerId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    SnowflakeIdWorker(long workerId) {
        if (workerId < 0 || workerId > MAX_WORKER_ID) {
            throw new IllegalArgumentException("Worker id must be between 0 and " + MAX_WORKER_ID);
        }
        this.workerId = workerId;
    }

    public synchronized long nextId() {
        long timestamp = System.currentTimeMillis();
        if (timestamp < lastTimestamp) {
            throw new IllegalStateException("Clock moved backwards, refusing to generate id");
        }
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                timestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }
        lastTimestamp = timestamp;
        return ((timestamp - EPOCH) << TIMESTAMP_SHIFT) | (workerId << WORKER_ID_SHIFT) | sequence;
    }

    private static long waitNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }

    private static long resolveWorkerId() {
        String value = System.getProperty("SNOWFLAKE_WORKER_ID", System.getenv("SNOWFLAKE_WORKER_ID"));
        return value != null ? Long.parseLong(value) : 0L;
    }
}
