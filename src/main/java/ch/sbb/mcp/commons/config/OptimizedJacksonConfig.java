package ch.sbb.mcp.commons.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/**
 * Optimized Jackson configuration for bandwidth-efficient JSON serialization.
 *
 * <p><strong>Optimizations:</strong></p>
 * <ul>
 *   <li><strong>Minified JSON Output:</strong> Disables pretty-printing (INDENT_OUTPUT = false)</li>
 *   <li><strong>Compact Writes:</strong> No extra whitespace in JSON output</li>
 *   <li><strong>Efficient Parsing:</strong> Ignores unknown properties to handle API evolution</li>
 *   <li><strong>Combined with gzip:</strong> 5-10% additional savings on top of 70-80% gzip compression</li>
 * </ul>
 *
 * <p><strong>Bandwidth Impact:</strong></p>
 * <ul>
 *   <li>Minification: ~5-10% reduction (removes whitespace/indentation)</li>
 *   <li>Gzip compression: ~70-80% reduction (handles JSON structure)</li>
 *   <li>Combined: ~75-85% total bandwidth reduction</li>
 * </ul>
 *
 * <p><strong>Usage:</strong></p>
 * <p>This configuration is automatically applied when sbb-mcp-commons is on the classpath.
 * It configures both the Spring Boot default ObjectMapper and WebFlux JSON codecs.</p>
 *
 * <p><strong>Note:</strong> While minification provides modest savings (5-10%), it's most
 * effective for large JSON payloads and when combined with gzip compression. For debugging,
 * you can temporarily enable pretty-printing via:
 * <pre>
 * spring.jackson.serialization.indent-output=true
 * </pre>
 */
@Configuration
@ConditionalOnClass(ObjectMapper.class)
public class OptimizedJacksonConfig implements WebFluxConfigurer {

    /**
     * Creates an optimized ObjectMapper bean for bandwidth-efficient JSON processing.
     *
     * <p>This ObjectMapper is configured for production use with:
     * <ul>
     *   <li>No pretty-printing (compact/minified output)</li>
     *   <li>Ignores unknown properties (forward compatibility)</li>
     *   <li>Fails on null for primitives (data integrity)</li>
     * </ul>
     *
     * @return optimized ObjectMapper instance
     */
    @Bean
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Serialization optimizations
        mapper.disable(SerializationFeature.INDENT_OUTPUT);  // Minify: no pretty-printing
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);  // Use ISO-8601
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);  // Allow empty objects

        // Deserialization optimizations
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);  // Ignore extra fields
        mapper.enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);  // Data integrity

        return mapper;
    }

    /**
     * Configures WebFlux HTTP message codecs to use the optimized ObjectMapper.
     *
     * <p>This ensures that all JSON encoding/decoding in WebFlux (for both
     * incoming requests and outgoing responses) uses the minified configuration.
     *
     * @param configurer the codec configurer to customize
     */
    @Override
    public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
        ObjectMapper mapper = objectMapper();

        configurer.defaultCodecs().jackson2JsonEncoder(
            new Jackson2JsonEncoder(mapper)
        );

        configurer.defaultCodecs().jackson2JsonDecoder(
            new Jackson2JsonDecoder(mapper)
        );
    }
}
