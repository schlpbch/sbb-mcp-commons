# SBB MCP Commons

Shared library for SBB MCP servers (journey-service-mcp, swiss-mobility-mcp).

## Overview

This library provides common infrastructure code that can be reused across multiple MCP server implementations:

- **Validation Framework**: Reusable validators for common input validation
- **Response Transformation**: Generic transformers for API response mapping
- **API Models**: Base classes for requests and responses
- **Exception Handling**: Common exception types

## Usage

### Maven Dependency

```xml
<dependency>
    <groupId>ch.sbb.mcp</groupId>
    <artifactId>sbb-mcp-commons</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### Validation Framework

```java
import ch.sbb.mcp.commons.validation.Validators;

public class MyTool {
    public void execute(String origin, String destination, int limit) {
        // Validate inputs
        Validators.requireNonEmpty(origin, "origin");
        Validators.requireNonEmpty(destination, "destination");
        Validators.requirePositive(limit, "limit");
        
        // Business logic...
    }
}
```

### Response Transformation

```java
import ch.sbb.mcp.commons.transformation.BaseResponseTransformer;

@Component
public class TripTransformer extends BaseResponseTransformer<ApiTrip, DomainTrip> {
    
    @Override
    public DomainTrip transform(ApiTrip apiTrip) {
        return DomainTrip.builder()
            .id(apiTrip.getId())
            .duration(formatDuration(apiTrip.getDurationMinutes()))
            .departureTime(formatDateTime(apiTrip.getDepartureTime()))
            .build();
    }
}
```

### API Models

```java
import ch.sbb.mcp.commons.model.BaseApiRequest;
import ch.sbb.mcp.commons.model.BaseApiResponse;

public class MyRequest extends BaseApiRequest {
    private String query;
    // Additional fields...
}

public class MyResponse extends BaseApiResponse {
    private List<Result> results;
    // Additional fields...
}
```

## Building

```bash
cd sbb-mcp-commons
mvn clean install
```

## Testing

```bash
mvn test
```

## Components

### Validation (`ch.sbb.mcp.commons.validation`)
- `Validators` - Static validation methods
- `ValidationException` - Exception for validation failures

### Transformation (`ch.sbb.mcp.commons.transformation`)
- `ResponseTransformer<SOURCE, TARGET>` - Generic transformer interface
- `BaseResponseTransformer<SOURCE, TARGET>` - Base class with utility methods

### Model (`ch.sbb.mcp.commons.model`)
- `BaseApiRequest` - Base class for API requests
- `BaseApiResponse` - Base class for API responses
- `ApiError` - Structured error information

### Handler (`ch.sbb.mcp.commons.handler`) - **Phase 2**
- `BaseToolHandler<INPUT, OUTPUT>` - Base class for MCP tool handlers
- `ToolResult` - Tool execution result wrapper

### Service (`ch.sbb.mcp.commons.service`) - **Phase 2**
- `BaseService` - Base class for service layer with metrics and retry patterns

### Client (`ch.sbb.mcp.commons.client`) - **Phase 3**
- `BaseApiClient<ERROR_TYPE>` - Base class for API clients with HTTP operations
- `WebClientFactory` - Factory for creating configured WebClient instances
- `ApiClientException` - Exception for API client errors

## Version

Current version: 1.0.0-SNAPSHOT

## License

SBB
