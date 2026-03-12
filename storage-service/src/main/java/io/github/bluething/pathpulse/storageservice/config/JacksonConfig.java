package io.github.bluething.pathpulse.storageservice.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

@Configuration
class JacksonConfig {
    @Bean
    @Primary
    public JsonMapper jsonMapper() {
        return JsonMapper.builder()

                // Serialization settings
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)    // ISO-8601 strings instead of timestamps
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)           // Don't fail on empty POJOs
                .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL))         // Skip null fields (or use NON_EMPTY, ALWAYS...)

                // Deserialization settings
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)  // Ignore unknown JSON fields (very common)
                .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY) // "value" → ["value"] auto-coercion
                .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES) // Avoid exceptions on null → int, etc.

                .build();
    }
}
