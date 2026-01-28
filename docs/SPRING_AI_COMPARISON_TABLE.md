# Spring AI vs sbb-mcp-commons - Feature Comparison

**Quick reference table for technical discussions**

---

## High-Level Comparison

| Aspect | sbb-mcp-commons v1.11.2 | Spring AI 2.0.0-M2 + utils | Winner |
|--------|------------------------|---------------------------|--------|
| **Development Speed** | Manual schemas, boilerplate | Auto-generated, annotations | **Spring AI** |
| **Maintenance Burden** | Full protocol + infrastructure | Infrastructure only | **Spring AI** |
| **Production Maturity** | Battle-tested (2 servers) | Newer (milestone) | **Current** |
| **Flexibility** | Full control | Spring conventions | **Current** |
| **Ecosystem Alignment** | Custom | Official Spring | **Spring AI** |
| **Infrastructure** | Complete | Need commons-utils | **Current** |

---

## Tool Development Comparison

### Current Approach (sbb-mcp-commons)

```java
@Component
public class CreateBookingTool extends BaseMcpTool<Input, JsonNode> {

    public record Input(String offerId, List<PassengerDetails> passengers) {}

    @Override
    protected Input validateAndParse(Map<String, Object> arguments) {
        String offerId = (String) arguments.get("offerId");
        Validators.requireNonEmpty(offerId, "offerId");
        // ... more validation
        return new Input(offerId, passengers);
    }

    @Override
    protected Mono<JsonNode> executeInternal(Input input) {
        return bookingService.createBooking(input)
            .map(objectMapper::valueToTree);
    }

    @Override
    public String inputSchema() {
        return """
        {
          "type": "object",
          "properties": {
            "offerId": {"type": "string", "description": "Offer ID"},
            "passengers": {"type": "array", "description": "Passenger list"}
          },
          "required": ["offerId", "passengers"]
        }
        """;  // ⚠️ Manual, error-prone
    }
}
```

**Lines of Code**: ~50
**Pain Points**: Manual schema, type casting, boilerplate

---

### Spring AI Approach

```java
@Component
public class BookingTools {

    @McpTool(
        name = "mobility__create_booking",
        description = "Create a confirmed ticket booking"
    )
    public Mono<JsonNode> createBooking(
        @McpToolParam(description = "Offer ID", required = true)
        String offerId,

        @McpToolParam(description = "Passenger details", required = true)
        List<PassengerDetails> passengers,

        McpAsyncRequestContext context
    ) {
        // Validation using commons-utils (preserved)
        Validators.requireNonEmpty(offerId, "offerId");

        return bookingService.createBooking(offerId, passengers)
            .map(objectMapper::valueToTree);
    }
    // ✅ JSON schema auto-generated from method signature
}
```

**Lines of Code**: ~20
**Benefits**: Auto schema, type safety, less boilerplate

---

## Detailed Feature Matrix

### MCP Protocol Features

| Feature | sbb-mcp-commons | Spring AI | Notes |
|---------|----------------|-----------|-------|
| **Tool Registration** | `McpTool` interface | `@McpTool` annotation | Spring AI simpler |
| **Manual Registry** | `McpToolRegistry.init()` | Auto-discovery | Spring AI automatic |
| **JSON Schema** | Manual string | Auto-generated | Spring AI safer |
| **Input Validation** | In `validateAndParse()` | External (commons-utils) | Same capability |
| **Type Safety** | `Map<String, Object>` | Typed parameters | Spring AI better |
| **Reactive Support** | `Mono<T>` / `Flux<T>` | `Mono<T>` / `Flux<T>` | Both equal |
| **Prompt Support** | `McpPromptProvider` | `@McpPrompt` | Spring AI simpler |
| **Resource Support** | `McpResource` interface | `@McpResource` | Spring AI simpler |
| **Transport Layer** | None (host implements) | Built-in (STDIO/SSE/HTTP) | Spring AI complete |
| **Request Context** | `McpRequestContext` | `McpAsyncRequestContext` | Both equal |
| **Protocol Models** | `McpRequest`, `McpResponse` | Spring AI models | Both equal |

**Score**: Spring AI wins 7/12 categories (protocol handling)

---

### Infrastructure Features

| Feature | sbb-mcp-commons | Spring AI 2.0.0-M2 | Migration Plan |
|---------|----------------|-------------------|----------------|
| **Validation Framework** | ✅ 15+ methods | ❌ None | **Keep in commons-utils** |
| **Email Validation** | ✅ `requireValidEmail()` | ❌ | Keep |
| **Date Validation** | ✅ `requireValidDate()` | ❌ | Keep |
| **Range Validation** | ✅ `requireInRange()` | ❌ | Keep |
| **Lat/Lon Validation** | ✅ `requireValidLatitude()` | ❌ | Keep |
| **API Client Base** | ✅ `BaseApiClient` | ❌ None | **Keep in commons-utils** |
| **Circuit Breaker** | ✅ Resilience4j | ❌ | Keep |
| **Retry Logic** | ✅ Exponential backoff | ❌ | Keep |
| **Rate Limiting** | ✅ `SimpleRateLimiter` | ❌ | Keep |
| **Redis Sessions** | ✅ `RedisMcpSessionStore` | ❌ Transport only | **Keep in commons-utils** |
| **Session TTL** | ✅ Configurable | ❌ | Keep |
| **Progress Tracking** | ✅ `ProgressTracker` | ⚠️ Context-based | **Keep in commons-utils** |
| **Progress Notifications** | ✅ `ProgressNotificationService` | ⚠️ | Keep |
| **Argument Extraction** | ✅ `ArgumentExtractor` | ❌ | **Keep in commons-utils** |
| **Date/Time Utils** | ✅ `DateTimeUtil` | ❌ | Keep |
| **GeoJSON Validation** | ✅ JTS-based | ❌ | Keep |
| **Compression** | ✅ Gzip filter | ❌ | Keep |
| **Response Transformers** | ✅ Interface | ❌ | Keep |
| **Health Indicators** | ✅ Actuator | ❌ | Keep |
| **Exception Handling** | ✅ `McpGlobalExceptionHandler` | ⚠️ Basic | **Keep in commons-utils** |

**Score**: sbb-mcp-commons wins 19/19 categories (infrastructure)

---

## Code Volume Comparison

### Current State (sbb-mcp-commons)

```
Total Classes: 48
Total Packages: 12
Total Tests: 196
Lines of Code: ~7,500

Protocol (would migrate):   ~2,500 LOC (33%)
Infrastructure (preserve): ~5,000 LOC (67%)
```

### After Migration

```
Spring AI:                     0 LOC (framework)
sbb-mcp-commons-utils:     ~5,000 LOC (67% preserved)
Tool definitions:          50% reduction per tool
```

---

## Performance Comparison

### Expected Changes

| Metric | Current | Spring AI | Change |
|--------|---------|-----------|--------|
| **Tool invocation** | Baseline | +10-20ms | Acceptable |
| **Schema validation** | Manual | Auto (faster) | Improvement |
| **Startup time** | Baseline | +100-200ms | Negligible |
| **Memory usage** | Baseline | +10-15MB | Acceptable |
| **Throughput** | Baseline | Similar | No impact |

**Validation Required**: POC must confirm <10% regression

---

## Migration Effort Comparison

### By Strategy

| Strategy | Duration | Team | Risk | Infrastructure Preserved |
|----------|----------|------|------|------------------------|
| **Hybrid (Recommended)** | 6-8 weeks | 2 @ 50% | Medium | 80% |
| **Complete Replacement** | 12-16 weeks | 2 @ 100% | High | 0% |
| **Status Quo** | 0 weeks | 0 | Low | 100% |

### By Component

| Component | Files | Effort | Complexity |
|-----------|-------|--------|------------|
| **Create commons-utils** | ~20 files | 2 weeks | Low |
| **Migrate tools** | 8 tools | 2 weeks | Medium |
| **Migrate resources** | 6 resources | 3 days | Low |
| **Migrate prompts** | 4 prompts | 2 days | Low |
| **Testing** | All | 2-3 weeks | High |

**Total**: 200 hours (~2 developers × 4 weeks at 50% time)

---

## Dependency Comparison

### Current

```xml
<!-- Single dependency -->
<dependency>
    <groupId>ch.sbb.mcp</groupId>
    <artifactId>sbb-mcp-commons</artifactId>
    <version>1.11.2</version>
</dependency>
```

### After Migration

```xml
<!-- Two dependencies -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-server-webflux</artifactId>
    <version>2.0.0-M2</version>
</dependency>

<dependency>
    <groupId>ch.sbb.mcp</groupId>
    <artifactId>sbb-mcp-commons-utils</artifactId>
    <version>2.0.0</version>
</dependency>
```

---

## Test Coverage Comparison

### Current (sbb-mcp-commons)

| Package | Coverage | Tests |
|---------|----------|-------|
| prompts | 100% | 15 |
| prompts.config | 100% | 8 |
| util | 94% | 20 |
| context | 92% | 10 |
| exception | 91% | 12 |
| validation | 85% | 25 |
| session | 80% | 18 |
| client | 70% | 15 |
| **Overall** | **54%** | **196** |

### After Migration (commons-utils)

| Package | Coverage | Tests |
|---------|----------|-------|
| validation | 85% | 25 |
| util | 94% | 20 |
| session | 80% | 18 |
| client | 70% | 15 |
| service | 85% | 12 |
| **Overall** | **82%** | **90+** |

**Note**: Protocol tests (~100 tests) no longer needed (Spring AI tested)

---

## Summary: What Changes, What Stays

### ✅ Migrates to Spring AI

**Why**: Spring AI does this better
- Tool registration mechanism
- Prompt registration mechanism
- Resource registration mechanism
- JSON-RPC protocol handling
- Transport layer (STDIO/SSE/HTTP)
- Auto-configuration for MCP

**Benefit**: Less maintenance, simpler code

---

### ✅ Stays in commons-utils

**Why**: Spring AI doesn't provide this
- Validation framework (15+ methods, heavily used)
- API client infrastructure (Resilience4j patterns)
- Redis session store (business state)
- Progress tracking (multi-step operations)
- Utilities (ArgumentExtractor, DateTimeUtil, GeoJSON)
- Exception handling (global handlers)

**Benefit**: Preserve battle-tested infrastructure

---

## Decision Summary

| Factor | Weight | Current | Spring AI | Hybrid |
|--------|--------|---------|-----------|--------|
| **Development Speed** | High | 6 | 9 | 9 |
| **Maintenance Burden** | High | 5 | 9 | 8 |
| **Production Stability** | Critical | 10 | 6 | 8 |
| **Infrastructure Quality** | High | 9 | 4 | 9 |
| **Ecosystem Alignment** | Medium | 5 | 10 | 9 |
| **Migration Risk** | High | 10 | 4 | 7 |
| **Total (weighted)** | - | **7.3** | **6.8** | **8.3** |

**Winner**: Hybrid approach balances all factors optimally

---

## Bottom Line

**Spring AI is better at**: Protocol, tool registration, developer experience
**sbb-mcp-commons is better at**: Infrastructure, validation, production patterns

**Hybrid approach gets the best of both worlds** ✅

---

**See also**:
- Full research: `SPRING_AI_MIGRATION_RESEARCH.md`
- Executive summary: `SPRING_AI_MIGRATION_EXECUTIVE_SUMMARY.md`
- Migration plan: `~/.claude/plans/composed-wishing-lemur.md`
