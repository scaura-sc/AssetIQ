package com.applicate.services.assetiq.util;

import com.applicate.services.assetiq.idgen.SnowflakeIdWorker;

/**
 * Mints short, human-readable, collision-free business codes (asset_number,
 * event_number, ...) from the same Snowflake sequence used for primary keys —
 * globally unique, no extra DB round trip, no race condition under concurrent
 * creates (unlike a naive "count existing rows + 1" scheme).
 */
public final class BusinessCodeGenerator {

    private BusinessCodeGenerator() {
    }

    public static String generate(String prefix) {
        return prefix + "-" + Long.toString(SnowflakeIdWorker.INSTANCE.nextId(), 36).toUpperCase();
    }
}
