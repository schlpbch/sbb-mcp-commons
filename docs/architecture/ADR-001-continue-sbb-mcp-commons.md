# ADR-001: Continue Using sbb-mcp-commons Over Spring AI MCP Server

**Status**: Accepted  
**Date**: 2026-01-13  
**Decision Makers**: Architecture Team  
**Related Documents**:

- [Spring AI MCP Evaluation](../SPRING_AI_EVALUATION.md)
- [Spring AI Gap Analysis](../SPRING_AI_GAP_ANALYSIS.md)

---

## Context

Spring AI (v1.1.1) introduced official MCP server support with annotation-based tool registration (`@McpTool`), multiple transport options (Stdio, HTTP, SSE), and Spring Boot starters. This raised the question: **Should we migrate from our custom `sbb-mcp-commons` library to Spring AI MCP server?**

### Current State

**sbb-mcp-commons v1.9.0** is a mature, production-proven library providing:

- Distributed session management (Redis + circuit breakers)
- Resilience patterns (Resilience4j integration)
- Comprehensive validation framework
- Reactive API client infrastructure
- GeoJSON/JTS geometry support
- Request context management
- Auto-configuration for Spring Boot

**Active Users**:

- `journey-service-mcp` (11 tools, 4 resources, 4 prompts)
- `swiss-mobility-mcp` (8 tools, 2 resources, 4 prompts)
- Integrated with Deno MCP Gateway and Astro UI dashboard

**Production Metrics**:

- 196 passing tests, 54% coverage (100% on critical paths)
- 2 production deployments on Google Cloud Run
- Zero downtime, proven reliability

---

## Decision

**We will continue using `sbb-mcp-commons` for existing production MCP servers and NOT migrate to Spring AI MCP server at this time.**

---

## Rationale

### 1. Critical Missing Features in Spring AI

Spring AI MCP server lacks essential production infrastructure:

| Feature | sbb-mcp-commons | Spring AI | Impact |
|---------|----------------|-----------|---------|
| **Session Management** | ✅ Redis + In-Memory | ❌ None | No user context |
| **Resilience Patterns** | ✅ Resilience4j | ❌ Manual | Cascading failures |
| **Validation Framework** | ✅ Comprehensive | ⚠️ Basic | Inconsistent errors |
| **API Client** | ✅ Built-in | ❌ Manual | Duplicate code |
| **GeoJSON Support** | ✅ JTS | ❌ None | No location tools |
| **Request Context** | ✅ ThreadLocal | ❌ None | Poor debugging |

**Detailed Analysis**: See [Spring AI Gap Analysis](../SPRING_AI_GAP_ANALYSIS.md) for 10 critical gaps with code examples.

### 2. High Migration Cost

**Effort Estimate**: 8-12 weeks of development

- Rebuild ~7,000+ lines of infrastructure code
- Update 500+ files across consuming projects
- Migrate 25+ session-dependent components
- Re-test all Cloud Run deployment patterns

**Risk Assessment**: 🔴 **HIGH**

- Production stability risk
- Potential service disruptions
- Unproven Spring AI deployment on Cloud Run
- Unknown performance characteristics

### 3. Limited Immediate ROI

**Benefits of Migration**:

- ✅ Official Spring support
- ✅ Annotation-based tool registration
- ✅ Dynamic tool updates at runtime

**Costs of Migration**:

- ❌ Rebuild session management
- ❌ Rebuild resilience patterns
- ❌ Rebuild validation framework
- ❌ Rebuild API client infrastructure
- ❌ Rebuild GeoJSON support
- ❌ High risk to production

**Conclusion**: Costs significantly outweigh benefits.

### 4. Production-Proven Stability

`sbb-mcp-commons` has demonstrated:

- ✅ Zero downtime in production
- ✅ Successful Cloud Run deployments
- ✅ Redis failover handling
- ✅ Circuit breaker protection
- ✅ Comprehensive error handling
- ✅ Health check integration

Spring AI MCP server is newer and less battle-tested in production environments.

### 5. Domain-Specific Features

`sbb-mcp-commons` provides Swiss transport-specific utilities:

- GeoJSON validation for location-based tools
- Swiss language validation (de, fr, it, en)
- SBB API client patterns
- Swiss coordinate system support

These would need to be rebuilt if migrating to Spring AI.

---

## Consequences

### Positive

1. **Maintain Production Stability** - Zero risk to existing services
2. **Preserve Investment** - Leverage 7,000+ lines of battle-tested code
3. **Keep Domain Features** - Retain Swiss transport-specific utilities
4. **Control Evolution** - Full control over features and fixes
5. **Proven Deployment** - Continue using established Cloud Run patterns

### Negative

1. **Maintenance Burden** - Must maintain custom library
2. **Community Support** - Smaller ecosystem than Spring AI
3. **Feature Gap** - May miss new Spring AI MCP features
4. **Standardization** - Not using industry-standard approach

### Neutral

1. **Monitor Spring AI** - Track maturity and feature additions
2. **Hybrid Approach** - Can use Spring AI for new servers
3. **Future Re-evaluation** - Reassess in Q3 2026

---

## Alternatives Considered

### Alternative 1: Full Migration to Spring AI

**Rejected** - High cost, high risk, limited ROI

### Alternative 2: Hybrid Approach

**Considered** - Use Spring AI for new servers, keep sbb-mcp-commons for existing

- **Status**: Viable for future new servers
- **Timeline**: 2026 H2 for new projects

### Alternative 3: Gradual Migration

**Rejected** - Complexity of maintaining two systems simultaneously

### Alternative 4: Extract Spring AI Features

**Considered** - Use Spring AI annotations alongside sbb-mcp-commons

- **Status**: Possible future enhancement
- **Complexity**: Medium

---

## Implementation Plan

### Immediate Actions (Q1 2026)

1. ✅ Document decision in ADR
2. ✅ Update README and CHANGELOG
3. ✅ Publish evaluation documents
4. ✅ Continue investing in sbb-mcp-commons

### Short-Term (Q2 2026)

1. Monitor Spring AI MCP server evolution
2. Track community adoption and maturity
3. Document any new Spring AI features of interest
4. Maintain sbb-mcp-commons for production services

### Medium-Term (Q3 2026)

1. **Re-evaluate** Spring AI MCP server when v2.0+ is released
2. Assess if critical gaps have been addressed:
   - Session management patterns
   - Resilience infrastructure
   - Production deployment guides
3. Update this ADR with findings

### Long-Term (2026 H2+)

1. Consider Spring AI for **new** MCP servers (greenfield)
2. Keep sbb-mcp-commons for existing servers (brownfield)
3. Potentially extract shared utilities to separate library
4. Evaluate hybrid architecture if beneficial

---

## Success Metrics

This decision will be considered successful if:

1. ✅ Zero production incidents related to MCP infrastructure
2. ✅ Continued development velocity on existing servers
3. ✅ Successful deployment of new features using sbb-mcp-commons
4. ✅ Maintained test coverage above 75%
5. ✅ Spring AI re-evaluation completed by Q3 2026

---

## Review Schedule

- **Next Review**: Q3 2026 (6 months)
- **Trigger for Early Review**: Spring AI v2.0 release or major feature additions
- **Review Criteria**:
  - Spring AI maturity level
  - Community adoption metrics
  - Gap closure progress
  - Business case for migration

---

## References

1. [Spring AI MCP Evaluation](../SPRING_AI_EVALUATION.md) - Executive summary
2. [Spring AI Gap Analysis](../SPRING_AI_GAP_ANALYSIS.md) - Technical deep-dive
3. [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
4. [sbb-mcp-commons Architecture](../architecture/README.md)
5. [Journey Service MCP](https://github.com/schlpbch/journey-service-mcp)
6. [Swiss Mobility MCP](https://github.com/schlpbch/swiss-mobility-mcp)

---

## Approval

**Approved By**: Architecture Team  
**Date**: 2026-01-13  
**Signature**: Documented in version control

---

## Changelog

| Date | Change | Author |
|------|--------|--------|
| 2026-01-13 | Initial ADR created | Architecture Team |
