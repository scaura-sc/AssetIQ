package com.applicate.services.assetiq.config;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Every {@code Long} field across the dto packages is a Snowflake-generated primary or
 * foreign key (id, assetId, oldAssetId, newAssetId, ...) — none are business quantities.
 * Snowflake IDs routinely exceed 2^53-1 (JS's Number.MAX_SAFE_INTEGER), so serializing them
 * as JSON numbers silently loses precision in every browser client: JSON.parse rounds to the
 * nearest representable double, and that rounded id then 404s the moment it's sent back in a
 * URL path. Serializing as strings avoids this. Request bodies are unaffected — Jackson
 * deserializes a JSON string into a Long field with no extra config needed.
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer longAsStringCustomizer() {
        return builder -> builder
                .serializerByType(Long.class, ToStringSerializer.instance)
                .serializerByType(long.class, ToStringSerializer.instance);
    }
}
