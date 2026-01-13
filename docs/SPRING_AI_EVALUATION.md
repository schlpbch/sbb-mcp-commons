# Spring AI MCP Server vs. sbb-mcp-commons: Migration Evaluation

**Date**: January 13, 2026  
**Current State**: `sbb-mcp-commons` v1.9.0 in production use  
**Evaluation**: Should we migrate to Spring AI MCP server?

---

## Executive Summary

**Recommendation: NO - Do not replace `sbb-mcp-commons` with Spring AI MCP server at this time.**

While Spring AI offers promising MCP server capabilities, `sbb-mcp-commons` is a mature, battle-tested library that provides significant value beyond what Spring AI currently offers. The migration would introduce substantial risk with limited immediate benefit.

---

## Current State Analysis

### sbb-mcp-commons (v1.9.0)

**Maturity**: Production-ready, battle-tested

- ✅ **196 passing tests** with 54% overall coverage (100% on critical paths)
- ✅ **2 production servers** actively using it (journey-service-mcp, swiss-mobility-mcp)
- ✅ **Proven deployment** on Google Cloud Run with complex infrastructure
- ✅ **Comprehensive ecosystem integration** with Deno gateway, Astro UI

**Core Capabilities**:

1. **Protocol Implementation** - Full MCP protocol support (tools, resources, prompts, sampling)
2. **Session Management** - Redis-backed distributed sessions with circuit breakers
3. **Reactive Architecture** - Project Reactor/WebFlux throughout
4. **Resilience Patterns** - Resilience4j integration for circuit breakers, retries, rate limiting
5. **Auto-Configuration** - Zero-config Spring Boot integration
6. **Validation Framework** - Comprehensive input validation utilities
7. **API Client Infrastructure** - Resilient HTTP client with retry/circuit breaker
8. **GeoJSON Support** - JTS-based geometry validation
9. **Resource Management** - Dynamic resource discovery and lifecycle
10. **Prompt Infrastructure** - Auto-discovery and registration
11. **Sampling Infrastructure** - Bidirectional LLM interaction
12. **Context Management** - Thread-local request context (Session ID, Correlation ID)

**Ecosystem Integration**:

- Integrated with **Deno MCP Gateway** (federation layer)
- Powers **Astro UI** dashboard
- Supports **SSE transport** for streaming
- **Cloud Run deployment patterns** (VPC, service accounts, health probes)
- **Multi-environment secrets** management

---

## Spring AI MCP Server Analysis

### Capabilities

**Strengths**:

1. ✅ **Official Spring support** - Part of the Spring AI ecosystem
2. ✅ **Annotation-based** - `@McpTool` for simplified tool exposure
3. ✅ **Multiple transports** - Stdio, HTTP (WebMVC/WebFlux), SSE
4. ✅ **Spring Boot starters** - `spring-ai-starter-mcp-server`, `spring-ai-starter-mcp-server-webflux`
5. ✅ **JSON schema generation** - Automatic tool input validation
6. ✅ **Dynamic tool updates** - Runtime tool addition/removal
7. ✅ **Capability negotiation** - Client-server feature detection
8. ✅ **Bidirectional AI** - Sampling support for LLM interaction
9. ✅ **Security** - OAuth 2.0 support via `spring-ai-community/mcp-security`

**Gaps Compared to sbb-mcp-commons**:

1. ❌ **No session management** - No built-in distributed session store
2. ❌ **No resilience patterns** - No circuit breaker/retry infrastructure
3. ❌ **No API client utilities** - No resilient HTTP client foundation
4. ❌ **No GeoJSON support** - No geometry validation utilities
5. ❌ **No validation framework** - No standardized input validators
6. ❌ **No context management** - No thread-local request context
7. ❌ **Limited production patterns** - Newer, less battle-tested
8. ❌ **No resource lifecycle** - Less mature resource management
9. ⚠️ **Maturity** - Spring AI 1.1.1 vs. sbb-mcp-commons 1.9.0 (production-hardened)

---

## Migration Impact Analysis

### Code Changes Required

**High Impact** (500+ files affected):

```
journey-service-mcp: ~150 imports from ch.sbb.mcp.commons
swiss-mobility-mcp: ~100 imports from ch.sbb.mcp.commons
sbb-mcp-gateway: Unknown (Java implementation)
```

**Components Requiring Replacement**:

1. **Session Management** → Build custom Redis integration
2. **McpToolRegistry** → Migrate to Spring AI annotations
3. **McpPromptHandler** → Rebuild prompt infrastructure
4. **McpResourceHandler** → Rebuild resource management
5. **BaseApiClient** → Replace with custom WebClient wrappers
6. **Validators** → Rebuild validation framework
7. **GeoJsonValidator** → Find alternative or rebuild
8. **ArgumentExtractor** → Rebuild type-safe extraction
9. **McpRequestContext** → Rebuild thread-local context
10. **Sampling infrastructure** → Migrate to Spring AI sampling

### Testing Impact

- **196 tests** in sbb-mcp-commons would need replacement
- **~300+ tests** across consuming projects would need updates
- **Integration tests** with Deno gateway would need verification
- **E2E tests** for Cloud Run deployments would need re-validation

### Deployment Risk

**Current Stability**:

- ✅ Proven Cloud Run deployment patterns
- ✅ VPC connector integration
- ✅ Redis session failover tested
- ✅ Health probe patterns established
- ✅ Multi-environment secret management

**Migration Risks**:

- ⚠️ Unproven Spring AI deployment on Cloud Run
- ⚠️ Potential transport compatibility issues (SSE)
- ⚠️ Unknown performance characteristics
- ⚠️ Regression risk in production services

---

## Comparison Matrix

| Feature | sbb-mcp-commons | Spring AI MCP | Winner |
|---------|----------------|---------------|--------|
| **MCP Protocol** | ✅ Full | ✅ Full | Tie |
| **Session Management** | ✅ Redis + In-Memory | ❌ None | **sbb-mcp-commons** |
| **Resilience** | ✅ Resilience4j | ❌ Manual | **sbb-mcp-commons** |
| **API Client** | ✅ Built-in | ❌ Manual | **sbb-mcp-commons** |
| **Validation** | ✅ Framework | ⚠️ Basic | **sbb-mcp-commons** |
| **GeoJSON** | ✅ JTS | ❌ None | **sbb-mcp-commons** |
| **Auto-Config** | ✅ Yes | ✅ Yes | Tie |
| **Annotations** | ❌ Manual | ✅ @McpTool | **Spring AI** |
| **Dynamic Tools** | ⚠️ Limited | ✅ Runtime | **Spring AI** |
| **Security** | ⚠️ Manual | ✅ OAuth2 | **Spring AI** |
| **Maturity** | ✅ Production | ⚠️ Newer | **sbb-mcp-commons** |
| **Testing** | ✅ 196 tests | ⚠️ Unknown | **sbb-mcp-commons** |
| **Documentation** | ✅ Extensive | ✅ Good | Tie |
| **Ecosystem** | ✅ Integrated | ⚠️ Standalone | **sbb-mcp-commons** |

**Score**: sbb-mcp-commons wins 9/14 categories

---

## Strategic Considerations

### Reasons to Keep sbb-mcp-commons

1. **Production Stability** - Zero downtime, proven reliability
2. **Ecosystem Lock-in** - Deep integration with gateway, UI, deployment patterns
3. **Feature Completeness** - Provides utilities Spring AI doesn't offer
4. **Migration Cost** - High effort, high risk, low immediate ROI
5. **Maintenance Control** - Full control over features and fixes
6. **Domain-Specific** - Tailored to SBB/Swiss transport needs

### Reasons to Consider Spring AI (Future)

1. **Official Support** - Spring team maintenance and updates
2. **Community** - Broader ecosystem and contributions
3. **Innovation** - Faster adoption of new MCP features
4. **Standardization** - Industry-standard approach
5. **Security** - Built-in OAuth2 support

---

## Recommendation

### Short-Term (2026 Q1-Q2): **KEEP sbb-mcp-commons**

**Actions**:

1. ✅ Continue using `sbb-mcp-commons` v1.9.0
2. ✅ Monitor Spring AI MCP server maturity
3. ✅ Evaluate Spring AI for **new** MCP servers (e.g., Python FastMCP alternatives)
4. ✅ Document migration path for future consideration

### Medium-Term (2026 Q3-Q4): **EVALUATE**

**Criteria for Re-evaluation**:

- Spring AI MCP server reaches v2.0+ (maturity)
- Session management patterns emerge in Spring AI
- Migration tooling becomes available
- Business case for standardization strengthens

### Long-Term (2027+): **CONSIDER HYBRID**

**Potential Strategy**:

1. **New servers** → Spring AI MCP (greenfield)
2. **Existing servers** → Keep sbb-mcp-commons (brownfield)
3. **Shared utilities** → Extract to separate library
4. **Gateway layer** → Abstract transport differences

---

## Alternative: Complementary Use

Instead of replacing, consider **complementary use**:

### Option 1: Feature Extraction

Extract specific Spring AI features:

- Use Spring AI's `@McpTool` annotations for new tools
- Keep sbb-mcp-commons for session/resilience/validation
- Bridge the two with adapters

### Option 2: New Server Pattern

- **journey-service-mcp** → Keep sbb-mcp-commons
- **swiss-mobility-mcp** → Keep sbb-mcp-commons
- **New MCP servers** → Evaluate Spring AI

### Option 3: Gradual Migration

1. Add Spring AI dependency alongside sbb-mcp-commons
2. Migrate tools one-by-one using annotations
3. Deprecate sbb-mcp-commons components gradually
4. Complete migration over 12-18 months

---

## Conclusion

**Do NOT replace sbb-mcp-commons with Spring AI MCP server now.**

The current library is production-proven, feature-complete, and deeply integrated into your ecosystem. Spring AI MCP server is promising but lacks critical features (session management, resilience patterns, validation framework) that your production services depend on.

**Recommended Path Forward**:

1. Continue investing in `sbb-mcp-commons` for existing servers
2. Monitor Spring AI MCP server evolution
3. Consider Spring AI for **new** MCP servers in 2026 H2
4. Re-evaluate in Q3 2026 when Spring AI matures

**Key Insight**: You built `sbb-mcp-commons` because you needed features that didn't exist elsewhere. Those needs haven't changed, and Spring AI doesn't yet fill all those gaps.
