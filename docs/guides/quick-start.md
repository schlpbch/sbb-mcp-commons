# Quick Start Guide

Get started with `sbb-mcp-commons` in 5 minutes!

## Prerequisites

- Java 25+
- Maven 3.x
- Spring Boot 3.2.3+

## Step 1: Add Dependency

Add to your `pom.xml`:

```xml
<dependency>
    <groupId>ch.sbb.mcp</groupId>
    <artifactId>sbb-mcp-commons</artifactId>
    <version>1.6.1</version>
</dependency>
```

## Step 2: Create Spring Boot Application

```java
@SpringBootApplication
public class MyMcpServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyMcpServerApplication.class, args);
    }
}
```

That's it! Auto-configuration handles the rest.

## Step 3: Define Your First Prompt

Create a prompt provider:

```java
@Component
public class MyPrompts implements McpPromptProvider {
    
    @Override
    public List<McpPrompt> getPrompts() {
        return List.of(
            new McpPrompt(
                "hello-world",
                "A simple greeting prompt",
                List.of(
                    new McpPromptArgument("name", "Person's name", true)
                ),
                "Hello, {name}! Welcome to MCP."
            )
        );
    }
}
```

## Step 4: Create MCP Controller

```java
@RestController
@RequestMapping("/mcp")
public class McpController {
    
    private final McpPromptHandler promptHandler;
    
    public McpController(McpPromptHandler promptHandler) {
        this.promptHandler = promptHandler;
    }
    
    @PostMapping
    public Mono<McpResponse> handleRequest(@RequestBody McpRequest request) {
        return switch (request.method()) {
            case "prompts/list" -> promptHandler.handlePromptsList(request);
            case "prompts/get" -> promptHandler.handlePromptsGet(request);
            default -> Mono.just(McpResponse.error(
                request.id(),
                McpResponse.McpError.methodNotFound(request.method())
            ));
        };
    }
}
```

## Step 5: Test It!

Start your application:

```bash
mvn spring-boot:run
```

Test the prompts/list endpoint:

```bash
curl -X POST http://localhost:8080/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": "1",
    "method": "prompts/list"
  }'
```

Response:

```json
{
  "jsonrpc": "2.0",
  "id": "1",
  "result": {
    "prompts": [
      {
        "name": "hello-world",
        "description": "A simple greeting prompt",
        "arguments": [
          {
            "name": "name",
            "description": "Person's name",
            "required": true
          }
        ]
      }
    ]
  }
}
```

Test the prompts/get endpoint:

```bash
curl -X POST http://localhost:8080/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": "2",
    "method": "prompts/get",
    "params": {
      "name": "hello-world",
      "arguments": {
        "name": "Alice"
      }
    }
  }'
```

Response:

```json
{
  "jsonrpc": "2.0",
  "id": "2",
  "result": {
    "name": "hello-world",
    "description": "A simple greeting prompt",
    "arguments": [...],
    "messages": [
      {
        "role": "user",
        "content": {
          "type": "text",
          "text": "Hello, Alice! Welcome to MCP."
        }
      }
    ]
  }
}
```

## Next Steps

- [Add Session Management](sessions.md) - Store stateful data
- [Implement Tools](tools.md) - Create MCP tools
- [Add Validation](validation.md) - Validate inputs
- [Configure Redis](redis.md) - Use Redis for sessions

## Common Patterns

### Adding Multiple Prompts

```java
@Component
public class JourneyPrompts implements McpPromptProvider {
    
    @Override
    public List<McpPrompt> getPrompts() {
        return List.of(
            findJourneysPrompt(),
            monitorStationPrompt(),
            findNearbyPrompt()
        );
    }
    
    private McpPrompt findJourneysPrompt() {
        return new McpPrompt(
            "find-journeys",
            "Find train journeys between stations",
            List.of(
                new McpPromptArgument("from", "Origin station", true),
                new McpPromptArgument("to", "Destination station", true),
                new McpPromptArgument("date", "Travel date (YYYY-MM-DD)", false)
            ),
            "Find train journeys from {from} to {to}" +
            (date != null ? " on {date}" : "")
        );
    }
    
    // More prompts...
}
```

### Using Validation

```java
@Component
public class JourneyTool {
    
    public Mono<ToolResult> findJourneys(String from, String to, Integer limit) {
        // Validate inputs
        Validators.requireNonEmpty(from, "from");
        Validators.requireNonEmpty(to, "to");
        Validators.requireInRange(limit, 1, 100, "limit");
        
        // Business logic...
        return journeyService.findJourneys(from, to, limit)
            .map(journeys -> ToolResult.success(journeys));
    }
}
```

### Using Sessions

```java
@Component
public class StatefulTool {
    
    private final McpSessionStore sessionStore;
    
    public Mono<ToolResult> processWithState(String sessionId, String data) {
        return sessionStore.getSession(sessionId)
            .switchIfEmpty(Mono.defer(() -> {
                // Create new session
                McpSession session = new McpSession(sessionId, Map.of("data", data));
                return sessionStore.createSession(sessionId, session)
                    .thenReturn(session);
            }))
            .flatMap(session -> {
                // Use session data
                Map<String, Object> state = session.state();
                // Process...
                return Mono.just(ToolResult.success(result));
            });
    }
}
```

## Troubleshooting

### Beans Not Auto-Configured

**Problem**: `McpPromptHandler` not found

**Solution**: Ensure `sbb-mcp-commons` is on the classpath and Spring Boot auto-configuration is enabled.

### Prompts Not Discovered

**Problem**: Prompts not appearing in `prompts/list`

**Solution**: 
1. Ensure your `McpPromptProvider` is a Spring `@Component`
2. Check it's in a package scanned by Spring Boot
3. Verify `getPrompts()` returns a non-empty list

### Redis Connection Errors

**Problem**: Session store fails with Redis errors

**Solution**:
1. Check Redis is running: `redis-cli ping`
2. Verify connection settings in `application.yml`
3. Use in-memory store for development (default when Redis unavailable)

## Example Project Structure

```
my-mcp-server/
├── src/main/java/
│   └── com/example/mcp/
│       ├── MyMcpServerApplication.java
│       ├── controller/
│       │   └── McpController.java
│       ├── prompts/
│       │   ├── JourneyPrompts.java
│       │   └── StationPrompts.java
│       └── tools/
│           ├── JourneyTool.java
│           └── StationTool.java
├── src/main/resources/
│   └── application.yml
└── pom.xml
```

## Configuration Example

```yaml
# application.yml
server:
  port: 8080

# Session configuration
mcp:
  session:
    ttl: PT2H                    # 2 hour session TTL
    
# Redis (optional)
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      
# Logging
logging:
  level:
    ch.sbb.mcp.commons: DEBUG
```

## What's Auto-Configured?

When you add `sbb-mcp-commons`, these beans are automatically created:

✅ `McpPromptRegistry` - Discovers and registers all prompts  
✅ `McpPromptHandler` - Handles prompt requests  
✅ `McpSessionStore` - In-memory or Redis-backed session storage  
✅ `ReactiveRedisTemplate<String, McpSession>` - (if Redis available)  
✅ Circuit Breaker for session operations  
✅ Retry policy for session operations  

No manual configuration needed!
