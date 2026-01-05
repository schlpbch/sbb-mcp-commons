package ch.sbb.mcp.commons.transformation;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Generic interface for transforming API responses to domain models.
 *
 * @param <SOURCE> the source type (API response)
 * @param <TARGET> the target type (domain model)
 */
public interface ResponseTransformer<SOURCE, TARGET> {
    
    /**
     * Transform a single source object to target type.
     *
     * @param source the source object
     * @return the transformed target object
     */
    TARGET transform(SOURCE source);
    
    /**
     * Transform a list of source objects to target type.
     *
     * @param sources the list of source objects
     * @return the list of transformed target objects
     */
    default List<TARGET> transformList(List<SOURCE> sources) {
        if (sources == null) {
            return List.of();
        }
        return sources.stream()
            .map(this::transform)
            .collect(Collectors.toList());
    }
}
