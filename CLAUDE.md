# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

SBB MCP Commons is a **shared infrastructure library** for building Model Context Protocol (MCP) servers. It provides reusable components for Spring Boot applications implementing MCP, including prompt infrastructure, session management, validation, and API client utilities.

**Key Technologies:**
- Java 21 with Spring Boot 3.2.3
- Project Reactor (Mono/Flux) for reactive programming
- Spring Data Redis (optional, for session storage)
- Resilience4j (circuit breaker, retry)
- Maven with JUnit 5
- JaCoCo for code coverage (196 tests, 54% coverage)

## Essential Build Commands

### Building and Testing
```bash
# Build and install to local Maven repository
mvn clean install

# Run all tests (196 tests)
mvn test

# Run specific test class
mvn test -Dtest=McpPromptHandlerTest

# Run tests with coverage report
mvn clean test
# Report at: target/site/jacoco/index.html

# Deploy to GitHub Packages (maintainers only)
mvn deploy
```

### Using This Library

Add to your project's `pom.xml`:
```xml
<dependency>
    <groupId>ch.sbb.mcp</groupId>
    <artifactId>sbb-mcp-commons</artifactId>
    <version>1.8.0</version>
</dependency>
```

Requires GitHub Packages authentication in `~/.m2/settings.xml`.

## Architecture Overview

### Package Structure
```
ch.sbb.mcp.commons/
├── core/                          # Core MCP abstractions
│   ├── McpTool.java              # Tool interface
│   ├── McpResult.java            # Result wrapper (Success/Failure)
│   └── McpError.java             # Error codes and messages
├── protocol/                      # MCP protocol models
│   ├── McpRequest.java           # JSON-RPC request
│   └── McpResponse.java          # JSON-RPC response with error codes
├── registry/
│   └── McpToolRegistry.java      # Auto-discovers and registers tools
├── prompts/                       # Prompt infrastructure
│   ├── McpPrompt.java            # Prompt model
│   ├── McpPromptArgument.java    # Prompt argument model
│   ├── McpPromptProvider.java    # Interface for defining prompts
│   ├── McpPromptRegistry.java    # Auto-discovers prompts
│   ├── McpPromptHandler.java     # Handles prompts/list and prompts/get
│   └── config/
│       └── McpPromptAutoConfiguration.java
├── resource/
│   └── McpResource.java          # Resource interface
├── handler/
│   ├── McpResourceHandler.java   # Handles resources/list and resources/read
│   ├── BaseToolHandler.java      # Base class for tool handlers
│   └── ToolResult.java           # Tool execution result
├── session/                       # Session management
│   ├── McpSession.java           # Session model
│   ├── McpSessionStore.java      # Session storage interface
│   ├── impl/
│   │   ├── InMemoryMcpSessionStore.java   # Default in-memory
│   │   └── RedisMcpSessionStore.java      # Redis-backed
│   └── config/
│       └── McpSessionAutoConfiguration.java
├── exception/                     # Exception handling
│   ├── McpException.java         # Base MCP exception
│   └── McpGlobalExceptionHandler.java
├── validation/
│   ├── Validators.java           # Input validation utilities
│   └── ValidationException.java
├── client/                        # API client utilities
│   ├── BaseApiClient.java        # Resilient HTTP client base
│   ├── WebClientFactory.java     # WebClient configuration
│   └── ApiClientException.java
├── context/
│   └── McpRequestContext.java    # Request context holder
├── util/
│   ├── DateTimeUtil.java         # Date/time parsing
│   └── ArgumentExtractor.java    # Type-safe argument extraction
├── geo/
│   └── GeoJsonValidator.java     # GeoJSON validation
├── sampling/                      # MCP sampling support
│   ├── McpSamplingClient.java
│   ├── SamplingRequest.java
│   └── SamplingResponse.java
├── ratelimit/
│   └── SimpleRateLimiter.java    # Simple rate limiting
├── model/                         # Base models
│   ├── BaseApiRequest.java
│   ├── BaseApiResponse.java
│   └── ApiError.java
├── service/
│   └── BaseService.java          # Base service class
└── transformation/
    ├── ResponseTransformer.java  # Response transformation interface
    └── BaseResponseTransformer.java
```

### Core Components

**MCP Protocol**
- `McpRequest` - JSON-RPC 2.0 request with `jsonrpc`, `id`, `method`, `params`
- `McpResponse` - JSON-RPC 2.0 response with success/error handling
- `McpResponse.McpError` - Standard JSON-RPC error codes (-32600 to -32700)

**Tool Infrastructure**
- `McpTool<T>` - Interface for implementing MCP tools
- `McpToolRegistry` - Auto-discovers `@Component` tools via Spring
- `McpToolRegistry.ToolInfo` - Tool metadata (name, summary, description, inputSchema)

**Prompt Infrastructure**
- `McpPromptProvider` - Interface for providing prompts
- `McpPromptRegistry` - Auto-discovers prompt providers
- `McpPromptHandler` - Handles `prompts/list` and `prompts/get` requests

**Resource Infrastructure**
- `McpResource` - Interface for MCP resources
- `McpResourceHandler` - Handles `resources/list` and `resources/read` requests

**Session Management**
- `McpSessionStore` - Reactive session storage interface
- `InMemoryMcpSessionStore` - Default in-memory implementation
- `RedisMcpSessionStore` - Redis-backed implementation (auto-configured)

## Development Guidelines

### Reactive Programming
- All public APIs return `Mono<T>` or `Flux<T>`
- NO `.block()` calls in production code
- Use `StepVerifier` for testing reactive streams

### Adding a New Component

1. **Create Interface** (if needed)
   ```java
   public interface MyComponent {
       Mono<Result> doSomething(Input input);
   }
   ```

2. **Create Implementation**
   ```java
   @Component
   @ConditionalOnMissingBean(MyComponent.class)
   public class DefaultMyComponent implements MyComponent {
       // Implementation
   }
   ```

3. **Add Auto-Configuration** (if needed)
   ```java
   @AutoConfiguration
   @ConditionalOnClass(MyComponent.class)
   public class MyComponentAutoConfiguration {
       @Bean
       @ConditionalOnMissingBean
       public MyComponent myComponent() {
           return new DefaultMyComponent();
       }
   }
   ```

4. **Write Tests** - Maintain >80% coverage for new code

### Implementing McpTool

```java
@Component
public class MyTool implements McpTool<MyResult> {
    @Override
    public String name() { return "myTool"; }

    @Override
    public String summary() { return "Short summary for tool list"; }

    @Override
    public String description() { return "Detailed description"; }

    @Override
    public String inputSchema() {
        return """
        {
          "type": "object",
          "properties": { "param1": {"type": "string"} },
          "required": ["param1"]
        }
        """;
    }

    @Override
    public Mono<MyResult> invoke(Map<String, Object> arguments) {
        String param1 = ArgumentExtractor.getString(arguments, "param1");
        // Implementation
    }
}
```

### Using Validators

```java
// In tool invoke() method
Validators.requireNonEmpty(origin, "origin");
Validators.requirePositive(limit, "limit");
Validators.requireValidDate(date, "date");
Validators.requireInRange(value, 1, 100, "value");
```

### Using ArgumentExtractor

```java
// Type-safe argument extraction with defaults
String origin = ArgumentExtractor.getString(args, "origin");
Integer limit = ArgumentExtractor.getInteger(args, "limit", 10);  // default 10
Boolean flag = ArgumentExtractor.getBoolean(args, "flag", false);
LocalDate date = ArgumentExtractor.getLocalDate(args, "date");
```

## Configuration

### Application Properties
```yaml
# Session Management
mcp.session.ttl: PT1H                          # Session TTL (default: 1 hour)
mcp.session.circuit-breaker.failure-rate-threshold: 50
mcp.session.circuit-breaker.wait-duration: 60s
mcp.session.retry.max-attempts: 3
mcp.session.retry.wait-duration: 100ms

# Redis (optional - auto-configured when available)
spring.data.redis.host: localhost
spring.data.redis.port: 6379
```

## Key Files

### Configuration
- `pom.xml` - Maven build configuration
- `src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` - Auto-configuration registration

### Documentation
- `README.md` - Library overview and quick start
- `CHANGELOG.md` - Version history
- `docs/` - Detailed documentation
  - `docs/architecture/` - Architecture overview
  - `docs/guides/` - Usage guides
  - `docs/api/` - API reference

## Error Codes

Standard JSON-RPC error codes used in `McpResponse.McpError`:
- `-32700` - Parse error
- `-32600` - Invalid request
- `-32601` - Method not found
- `-32602` - Invalid params
- `-32603` - Internal error

Custom MCP error codes in `McpError.ErrorCode`:
- `NOT_FOUND` - Resource not found
- `INVALID_INPUT` - Invalid input parameters
- `EXTERNAL_API_ERROR` - External API failure
- `TIMEOUT` - Request timeout
- `RATE_LIMITED` - Rate limit exceeded
- `INTERNAL_ERROR` - Internal server error
- `SERVICE_UNAVAILABLE` - Service unavailable
- `UNAUTHORIZED` - Authentication/authorization error

## Testing

```bash
# Run all 196 tests
mvn test

# Run specific test class
mvn test -Dtest=McpPromptHandlerTest

# Run tests matching pattern
mvn test -Dtest=*PromptTest

# Generate coverage report
mvn clean test jacoco:report
```

**Coverage by Package:**
- `prompts` - 100%
- `prompts.config` - 100%
- `util` - 94%
- `context` - 92%
- `exception` - 91%
- `validation` - 85%
- `session` - 80%
- `client` - 70%

## Publishing (Maintainers)

```bash
# Update version in pom.xml
# Update CHANGELOG.md

# Run tests
mvn clean test

# Deploy to GitHub Packages
mvn deploy

# Create GitHub release tag
git tag v1.x.0
git push origin v1.x.0
```

## Related Projects

- [journey-service-mcp](https://github.com/schlpbch/journey-service-mcp) - Journey planning MCP server (uses this library)
- [swiss-mobility-mcp](https://github.com/schlpbch/swiss-mobility-mcp) - Ticketing/booking MCP server (uses this library)
