# Spring AI Migration Research Summary

**Date**: January 28, 2026
**Status**: Research Complete - Decision Pending
**Prepared For**: Technical Leadership & Architecture Team

---

## Executive Summary

### Research Question
Should we migrate from our custom **sbb-mcp-commons** library to **Spring AI 2.0.0-M2** MCP server framework?

### Recommendation
**Hybrid Approach**: Migrate MCP protocol handling to Spring AI while preserving our infrastructure utilities in a new `sbb-mcp-commons-utils` library.

### Key Benefits
- **Reduced Maintenance**: Spring team handles MCP protocol updates
- **Simplified Development**: Annotation-based tool registration (`@McpTool`)
- **Preserved Investment**: Keep battle-tested infrastructure (validation, API clients, Redis sessions)
- **Balance**: Innovation with stability

### Timeline & Risk
- **POC Phase**: 1 week to validate feasibility
- **Full Migration**: 6-8 weeks (if POC succeeds)
- **Risk Level**: Medium (with clear rollback plan)

---

## Current State Analysis

### sbb-mcp-commons v1.11.2

**Scope**: 48 Java classes, 12 packages, 196 tests (54% coverage)

**What It Provides**:

**MCP Protocol (Would migrate to Spring AI)**:
- Tool registration via `McpTool` interface + `McpToolRegistry`
- Prompt infrastructure via `McpPromptProvider` + `McpPromptHandler`
- Resource management via `McpResource` + `McpResourceHandler`
- JSON-RPC protocol models (`McpRequest`, `McpResponse`)

**Infrastructure (Would preserve in commons-utils)**:
- ✅ **Validation Framework**: `Validators` class with 15+ methods (85% test coverage)
- ✅ **API Client**: `BaseApiClient` with Resilience4j (circuit breaker, retry, rate limiting)
- ✅ **Redis Sessions**: `RedisMcpSessionStore` for distributed session state
- ✅ **Progress Tracking**: `ProgressTracker` + `ProgressNotificationService`
- ✅ **Utilities**: `ArgumentExtractor`, `DateTimeUtil`, `GeoJsonValidator`
- ✅ **Exception Handling**: `McpGlobalExceptionHandler`, `McpException`

**Current Consumers**:
- **swiss-mobility-mcp**: 8 tools, 6 resources, 4 prompts
- **journey-service-mcp**: ~10 tools, multiple resources

---

## Spring AI 2.0.0-M2 Analysis

### What Spring AI Provides

**MCP Protocol (Better than ours)**:
- ✅ `@McpTool` annotation with **auto JSON schema generation**
- ✅ `@McpPrompt`, `@McpResource` annotations
- ✅ Built-in transports (STDIO, SSE, HTTP/WebFlux)
- ✅ Boot starters for zero-config setup
- ✅ Request context injection (`McpAsyncRequestContext`)
- ✅ Progress tracking, logging notifications
- ✅ Official Spring team support & maintenance

**What Spring AI Does NOT Provide**:
- ❌ No validation framework (no equivalent to our `Validators`)
- ❌ No API client utilities (no `BaseApiClient`, Resilience4j patterns)
- ❌ No Redis session store (only transport-level sessions)
- ❌ No GeoJSON validation, rate limiting, compression filters
- ❌ No response transformers, health indicators

### Capability Comparison Matrix

| Feature | sbb-mcp-commons | Spring AI 2.0.0-M2 | Winner |
|---------|----------------|-------------------|--------|
| **Tool Registration** | Interface-based | `@McpTool` annotation | **Spring AI** (simpler) |
| **JSON Schema** | Manual string | Auto-generated | **Spring AI** (less error-prone) |
| **Transport Layer** | Host implements | Built-in (STDIO/SSE/HTTP) | **Spring AI** (complete) |
| **Validation Framework** | 15+ methods | None | **sbb-mcp-commons** |
| **API Client** | BaseApiClient + Resilience4j | None | **sbb-mcp-commons** |
| **Redis Sessions** | RedisMcpSessionStore | None | **sbb-mcp-commons** |
| **Progress Tracking** | Full infrastructure | Context-based | **Tie** (different approaches) |
| **Spring Boot Integration** | Auto-config | Boot starters | **Tie** |
| **Production Maturity** | Battle-tested (2 servers) | Newer (M2 milestone) | **sbb-mcp-commons** |

**Score**: MCP Protocol (Spring AI wins 3/3), Infrastructure (sbb-mcp-commons wins 5/5)

---

## Migration Strategies Evaluated

### Strategy A: Hybrid "Best of Both Worlds" ✅ RECOMMENDED

**Approach**:
- Migrate MCP protocol to Spring AI
- Extract utilities to new `sbb-mcp-commons-utils` v2.0.0

**What Migrates**:
```
McpTool interface          → @McpTool annotation
McpPromptProvider          → @McpPrompt annotation
McpResource                → @McpResource annotation
McpToolRegistry            → Spring AI auto-discovery
BaseMcpController          → Spring AI transports
```

**What Stays (commons-utils)**:
```
Validators                 → ch.sbb.mcp.commons.utils.validation
BaseApiClient              → ch.sbb.mcp.commons.utils.client
RedisMcpSessionStore       → ch.sbb.mcp.commons.utils.session
ProgressTracker            → ch.sbb.mcp.commons.utils.service
ArgumentExtractor          → ch.sbb.mcp.commons.utils.util
McpGlobalExceptionHandler  → ch.sbb.mcp.commons.utils.exception
```

**Pros**:
- ✅ Reduces MCP protocol maintenance burden
- ✅ Preserves 80% of existing infrastructure value
- ✅ Clear separation: protocol vs utilities
- ✅ Simpler tool development with annotations
- ✅ Manageable timeline (6-8 weeks)

**Cons**:
- ⚠️ Still maintain a utility library
- ⚠️ Dual dependency (Spring AI + commons-utils)

**Timeline**: 6-8 weeks
**Risk**: Medium

---

### Strategy B: Complete Replacement ❌ NOT RECOMMENDED

**Approach**: Full migration to Spring AI, rebuild all infrastructure

**Why Not**:
- Would lose Resilience4j patterns (circuit breaker, retry)
- Would lose Redis session store
- 12-16 weeks effort to rebuild infrastructure
- High risk to production stability
- Limited business value

**Timeline**: 12-16 weeks
**Risk**: High

---

### Strategy C: Status Quo ⏸️ FALLBACK

**Approach**: Continue with sbb-mcp-commons, no migration

**When to Use**: If Spring AI POC reveals blocking issues

**Timeline**: N/A
**Risk**: Low

---

## Recommended Migration Path (Hybrid)

### Phase 0: Proof of Concept (1 week)

**Goal**: Validate Spring AI 2.0.0-M2 meets requirements

**Create**: Small test project with:
- 1 simple tool (`@McpTool` with primitives)
- 1 complex tool (nested objects, validation, progress tracking)
- 1 resource (`@McpResource`)
- 1 prompt (`@McpPrompt`)

**Validate**:
- JSON schema auto-generation quality
- Reactive `Mono<T>` support
- Integration with commons-utils validation
- Progress tracking integration
- Performance baseline (<50ms overhead)

**Success Criteria**:
- ✅ All components register and work correctly
- ✅ JSON schemas are correct and complete
- ✅ Validation integrates seamlessly
- ✅ No blocking bugs in Spring AI

**Failure Criteria (Abort Migration)**:
- ❌ Spring AI doesn't support Mono<T>
- ❌ JSON schema generation inadequate
- ❌ Cannot integrate validation
- ❌ Performance regression >20%

---

### Phase 1: Library Restructuring (2 weeks)

**Goal**: Create `sbb-mcp-commons-utils` v2.0.0

**Extract to New Library**:
```
sbb-mcp-commons-utils/
├── validation/    (Validators, ValidationException)
├── client/        (BaseApiClient, WebClientFactory, Compression)
├── session/       (McpSession, RedisMcpSessionStore)
├── service/       (ProgressTracker, ProgressNotificationService)
├── util/          (ArgumentExtractor, DateTimeUtil, GeoJsonValidator)
├── transformation/(ResponseTransformer, BaseResponseTransformer)
└── exception/     (McpGlobalExceptionHandler, McpException)
```

**Testing**: Migrate 80+ utility tests, maintain 85%+ coverage

---

### Phase 2: Tool Migration (2 weeks)

**Goal**: Migrate swiss-mobility-mcp tools to Spring AI

**Migration Pattern**:

**BEFORE (Interface-based)**:
```java
@Component
public class CreateBookingTool extends BaseMcpTool<Input, JsonNode> {
    public record Input(String offerId, List<PassengerDetails> passengers) {}

    @Override
    protected Input validateAndParse(Map<String, Object> args) {
        String offerId = (String) args.get("offerId");
        Validators.requireNonEmpty(offerId, "offerId");
        return new Input(offerId, passengers);
    }

    @Override
    protected Mono<JsonNode> executeInternal(Input input) {
        return bookingService.createBooking(input);
    }

    @Override
    public String inputSchema() {
        return """{"type": "object", "properties": {...}}""";
    }
}
```

**AFTER (Annotation-based)**:
```java
@Component
public class BookingTools {

    @McpTool(
        name = "mobility__create_booking",
        description = "Create a confirmed ticket booking"
    )
    public Mono<JsonNode> createBooking(
        @McpToolParam(description = "Offer ID", required = true) String offerId,
        @McpToolParam(description = "Passengers", required = true) List<PassengerDetails> passengers,
        McpAsyncRequestContext context
    ) {
        // Validation using commons-utils
        Validators.requireNonEmpty(offerId, "offerId");

        // Progress tracking using commons-utils
        String sessionId = context.getMeta().get("sessionId").toString();
        ProgressTracker tracker = progressService.createTracker(sessionId, "createBooking", 2);

        return bookingService.createBooking(offerId, passengers)
            .doOnNext(b -> tracker.step("Booking created"))
            .doFinally(sig -> tracker.complete());
    }
}
```

**Key Improvements**:
1. No manual JSON schema - auto-generated from method signature
2. Type-safe parameters (no `Map<String, Object>`)
3. Still use validation from commons-utils
4. Still use progress tracking from commons-utils

**Migrate**: 8 tools in swiss-mobility-mcp

---

### Phase 3: Resources & Prompts (1 week)

**Goal**: Migrate 6 resources and 4 prompts to annotations

**Resource Migration**:
```java
// BEFORE
@Component
public class ServiceStatusResource implements McpResource {
    @Override public String getResourceUri() { return "sbb://..."; }
    @Override public Mono<Object> readResource() { ... }
}

// AFTER
@Component
public class SwissMobilityResources {
    @McpResource(uri = "sbb://swiss-mobility/service-status", ...)
    public Mono<ServiceStatus> getServiceStatus() { ... }
}
```

---

### Phase 4: Testing & Validation (2-3 weeks)

**Goal**: Ensure production readiness

**Testing**:
- Unit tests (196+ tests migrated)
- Integration tests (Claude Desktop)
- Performance testing (load tests)
- End-to-end workflows

**Validation**:
- All tools/resources/prompts working
- Redis sessions functioning
- Progress tracking operational
- Performance within 10% of baseline

---

## Session Management Strategy

### Challenge: Two Types of Sessions

**Transport Sessions (Spring AI)**:
- Connection-scoped (WebSocket/SSE lifecycle)
- Short-lived
- Managed by Spring AI

**Business Sessions (commons-utils)**:
- Conversation state across requests
- Long-lived (1 hour TTL)
- Redis-backed for horizontal scaling
- Progress tracking, user preferences

### Solution: Dual-Layer Architecture

```java
@McpTool(name = "create_booking", ...)
public Mono<Result> createBooking(
    @McpToolParam(required = true) String offerId,
    McpAsyncRequestContext transportContext  // Spring AI transport session
) {
    // Extract business session ID from transport context
    String businessSessionId = transportContext.getMeta()
        .getOrDefault("businessSessionId", UUID.randomUUID().toString())
        .toString();

    // Get business session from Redis (commons-utils)
    return businessSessionStore.getSession(businessSessionId)
        .flatMap(session -> {
            // Access business state, progress tracking
            ProgressTracker tracker = progressService.createTracker(...);
            // ... business logic
        });
}
```

**Why This Works**:
- Spring AI handles connection lifecycle
- commons-utils handles business state persistence
- Clear separation of concerns

---

## Risk Assessment

### Critical Risks

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|------------|
| Spring AI 2.0.0-M2 bugs | Medium | Critical | POC validation first; pin version; rollback plan |
| JSON schema issues | Medium | High | Compare generated vs manual; integration tests |
| Session complexity | High | High | Dual-layer design; extensive testing |
| Performance regression | Low | Medium | Benchmark before/after; <10% acceptable |
| Tool discovery failures | Low | High | Integration tests for all tools |

### Rollback Strategy

**Feature Toggle**:
```xml
<profiles>
    <profile>
        <id>legacy</id>
        <!-- Use sbb-mcp-commons v1.11.2 -->
    </profile>
    <profile>
        <id>spring-ai</id>
        <!-- Use Spring AI 2.0.0-M2 + commons-utils -->
    </profile>
</profiles>
```

**Quick Rollback**:
```bash
mvn clean install -P legacy
# Immediate rollback to previous version
```

---

## Cost-Benefit Analysis

### Benefits

**Development Efficiency**:
- 📉 50% less boilerplate (no manual JSON schemas)
- 📉 Faster tool development with annotations
- 📈 Spring team handles protocol updates

**Maintenance**:
- 📉 Reduced maintenance burden for MCP protocol
- 📉 Fewer breaking changes (Spring AI versioning)
- 📈 Official Spring support

**Developer Experience**:
- 📈 Simpler, more intuitive API
- 📈 Better IDE support (annotation-based)
- 📈 Consistent with Spring ecosystem patterns

### Costs

**Migration Effort**:
- 1 week POC
- 6-8 weeks implementation
- ~2 developers at 50% time

**Ongoing**:
- Maintain commons-utils library
- Dual dependency management
- Learning curve for Spring AI patterns

**Risk**:
- Medium risk (mitigated by POC)
- Potential performance regression (benchmark required)
- Spring AI maturity (milestone release)

---

## Decision Framework

### GO: Proceed with Migration

**Conditions**:
- ✅ POC validates all functionality
- ✅ JSON schema quality acceptable
- ✅ Performance within 10% baseline
- ✅ Spring AI stable (no blockers)
- ✅ Team has 6-8 weeks capacity
- ✅ Business value clear

**Benefits**: Reduced maintenance, simpler development, Spring ecosystem alignment

---

### NO-GO: Stay with sbb-mcp-commons

**Conditions**:
- ❌ Spring AI has blocking bugs
- ❌ JSON schema generation inadequate
- ❌ Performance regression >20%
- ❌ Cannot integrate commons-utils
- ❌ Timeline conflicts with priorities

**Benefits**: Zero risk, proven stability, no migration cost

---

### DEFER: Postpone Decision

**Conditions**:
- ⏸️ Spring AI not yet GA
- ⏸️ Need more POC validation
- ⏸️ Awaiting bug fixes
- ⏸️ Team capacity constraints

**Benefits**: Wait for Spring AI maturity, more information

---

## Recommended Next Steps

### Immediate (Next 1-2 Weeks)

**1. POC Implementation**
- **Owner**: Lead developer
- **Duration**: 2 days
- **Output**: Working POC with 4 components

```bash
# Create POC project
mkdir spring-ai-poc
cd spring-ai-poc

# Dependencies: Spring Boot 3.5, Spring AI 2.0.0-M2, sbb-mcp-commons-utils
# Implement: 2 tools, 1 resource, 1 prompt
# Test: Registration, schema generation, validation, progress
```

**2. POC Review & Decision**
- **Owner**: Tech lead + architecture team
- **Duration**: 1 day
- **Output**: Go/No-Go decision

**Review Checklist**:
- [ ] All POC components work
- [ ] JSON schemas correct
- [ ] Performance acceptable
- [ ] Commons-utils integration verified
- [ ] No blocking issues

---

### If GO Decision (Weeks 2-9)

**Week 2-3: Library Restructuring**
- Create sbb-mcp-commons-utils v2.0.0
- Extract utilities (validation, client, session)
- Migrate 80+ tests
- Publish to GitHub Packages

**Week 4-5: Tool Migration**
- Migrate swiss-mobility-mcp (8 tools)
- Update tests
- Integration testing

**Week 6: Resources & Prompts**
- Migrate 6 resources
- Migrate 4 prompts
- End-to-end testing

**Week 7-8: Journey Service Migration**
- Migrate journey-service-mcp (~10 tools)
- Full integration testing

**Week 9: Production Rollout**
- Blue-green deployment
- Monitor for issues
- Keep rollback option for 1 week

---

### If NO-GO Decision

**Document Reasons**:
- Specific issues found in POC
- Performance problems
- Integration challenges

**Define Re-evaluation Criteria**:
- Spring AI GA release
- Bug fixes in Spring AI
- When issues resolved

**Continue Evolution**:
- Keep improving sbb-mcp-commons
- Monitor Spring AI progress
- Re-evaluate in 6 months

---

## Technical Details for POC

### POC Project Structure

```
spring-ai-poc/
├── pom.xml (Spring Boot 3.5, Spring AI 2.0.0-M2)
├── src/main/java/
│   ├── SpringAiPocApplication.java
│   ├── tools/
│   │   └── PocTools.java (@McpTool annotations)
│   ├── resources/
│   │   └── PocResources.java (@McpResource)
│   └── prompts/
│       └── PocPrompts.java (@McpPrompt)
└── src/test/java/
    └── PocIntegrationTest.java
```

### POC Dependencies

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-starter-mcp-server-webflux</artifactId>
        <version>2.0.0-M2</version>
    </dependency>

    <dependency>
        <groupId>ch.sbb.mcp</groupId>
        <artifactId>sbb-mcp-commons-utils</artifactId>
        <version>2.0.0-SNAPSHOT</version>
    </dependency>
</dependencies>
```

### POC Test Scenarios

**1. Simple Tool Test**:
```java
@McpTool(name = "add_numbers", description = "Add two numbers")
public Mono<Integer> addNumbers(
    @McpToolParam(required = true) int a,
    @McpToolParam(required = true) int b
) {
    return Mono.just(a + b);
}

// Test: Verify JSON schema includes both parameters
// Test: Invoke and verify result
```

**2. Complex Tool Test**:
```java
@McpTool(name = "poc__create_booking", ...)
public Mono<JsonNode> createBooking(
    @McpToolParam(required = true) String offerId,
    @McpToolParam(required = true) Integer passengerCount,
    McpAsyncRequestContext context
) {
    // Validate with commons-utils
    validators.requireNonEmpty(offerId, "offerId");
    validators.requireInRange(passengerCount, 1, 10, "passengerCount");

    // Progress tracking with commons-utils
    ProgressTracker tracker = progressService.createTracker(...);

    // Business logic
    return Mono.just(offerId)
        .doOnNext(id -> tracker.step("Step 1"))
        .map(this::createResponse);
}

// Test: Validation errors throw correct exceptions
// Test: Progress notifications sent
// Test: JSON schema includes all parameters
```

**3. Performance Test**:
```java
// Baseline: Current implementation with sbb-mcp-commons
// Compare: Spring AI implementation
// Acceptable: <50ms overhead for POC, <10% for production
```

---

## Success Metrics

### POC Phase
- [ ] All 4 components register successfully
- [ ] JSON schemas match manual schemas (quality check)
- [ ] Validation errors handled correctly
- [ ] Progress tracking works with Spring AI context
- [ ] Performance overhead <50ms
- [ ] No blocking bugs

### Implementation Phase
- [ ] All 8 tools migrated (swiss-mobility-mcp)
- [ ] All 6 resources migrated
- [ ] All 4 prompts migrated
- [ ] 196+ tests passing
- [ ] Performance within 10% of baseline
- [ ] Integration with Claude Desktop working
- [ ] Production deployment successful

---

## Conclusion

### Recommendation: Hybrid Approach

**Rationale**:
1. **Reduces maintenance**: Spring AI handles MCP protocol evolution
2. **Preserves value**: Keep battle-tested infrastructure (80% of code)
3. **Balances risk**: Use Spring AI where mature, keep utils where unique
4. **Clear path**: POC validates feasibility with minimal investment

### Next Action: POC

**Immediate next step**: Create POC to validate Spring AI 2.0.0-M2

**Timeline**: 2 days for POC, then decision point

**Decision Point**: After POC review, decide:
- ✅ GO: Proceed with full migration (6-8 weeks)
- ❌ NO-GO: Stay with sbb-mcp-commons
- ⏸️ DEFER: Wait for Spring AI maturity

---

## Appendix: Key Resources

### Documentation
- [Spring AI MCP Overview](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-overview.html)
- [MCP Server Annotations](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-annotations-server.html)
- [Spring AI Boot Starter](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-server-boot-starter-docs.html)

### Related Documents
- `docs/SPRING_AI_EVALUATION.md` - Original evaluation (January 2026)
- `docs/SPRING_AI_GAP_ANALYSIS.md` - Detailed gap analysis
- `CLAUDE.md` - Project context for Claude Code

### Contact
- **Technical Lead**: [Name]
- **Architecture Team**: [Names]
- **Questions**: File issue in GitHub or Slack #mcp-migration

---

**Document Version**: 1.0
**Last Updated**: January 28, 2026
**Status**: Ready for Team Review
