# Feature Request: Migrate to Spring AI MCP Server

## Summary

Migrate sbb-mcp-commons to use Spring AI 2.0.0-M2 MCP server framework while preserving infrastructure utilities in a new `sbb-mcp-commons-utils` library.

---

## Problem Statement

### Current Challenges

1. **Maintenance Burden**: We maintain the entire MCP protocol implementation ourselves, requiring ongoing updates as the protocol evolves
2. **Boilerplate Code**: Each tool requires ~50 lines of boilerplate including manual JSON schema definitions
3. **Error-Prone Schemas**: Manual JSON schema strings are prone to typos and drift from implementation
4. **Protocol Updates**: Must manually track and implement MCP specification changes
5. **Ecosystem Alignment**: Custom implementation doesn't align with emerging Spring ecosystem standards

### Impact

- Development team spends ~20% time maintaining MCP protocol
- New tools take longer to implement due to boilerplate
- Risk of schema/implementation mismatches
- Miss out on Spring AI ecosystem improvements

---

## Proposed Solution

### Hybrid Approach: Best of Both Worlds

**Migrate to Spring AI**:
- Tool registration: `McpTool` → `@McpTool` annotations
- Prompt registration: `McpPromptProvider` → `@McpPrompt` annotations
- Resource registration: `McpResource` → `@McpResource` annotations
- Protocol handling: Let Spring AI manage JSON-RPC
- Transport layer: Use Spring AI's built-in transports

**Preserve in `sbb-mcp-commons-utils` v2.0.0**:
- ✅ Validation framework (`Validators` - 15+ methods)
- ✅ API client infrastructure (`BaseApiClient` + Resilience4j)
- ✅ Redis session store (`RedisMcpSessionStore`)
- ✅ Progress tracking (`ProgressTracker`, `ProgressNotificationService`)
- ✅ Utilities (`ArgumentExtractor`, `DateTimeUtil`, `GeoJsonValidator`)
- ✅ Exception handling (`McpGlobalExceptionHandler`)

---

## Benefits

### Developer Experience
- 📉 **50% less boilerplate**: Tool definitions reduced from ~50 to ~20 lines
- ✅ **Auto-generated schemas**: No more manual JSON schema strings
- ✅ **Type safety**: Typed method parameters instead of `Map<String, Object>`
- ✅ **IDE support**: Better autocomplete and validation with annotations

### Maintenance
- 📉 **Reduced burden**: Spring team maintains MCP protocol updates
- 📉 **Fewer breaking changes**: Spring AI versioning vs custom protocol tracking
- ✅ **Official support**: Part of official Spring ecosystem
- ✅ **Community**: Benefit from broader Spring AI community

### Strategic
- ✅ **Ecosystem alignment**: Consistent with Spring Boot conventions
- ✅ **Future-proof**: Spring team drives MCP integration
- ✅ **Preserved value**: Keep 80% of infrastructure investment

---

## Implementation Plan

### Phase 0: Proof of Concept (Week 1) 🔬

**Goal**: Validate Spring AI 2.0.0-M2 meets requirements

**Tasks**:
- [ ] Create POC project with Spring AI + commons-utils
- [ ] Implement 1 simple tool (primitives only)
- [ ] Implement 1 complex tool (validation, progress tracking)
- [ ] Implement 1 resource and 1 prompt
- [ ] Validate JSON schema generation quality
- [ ] Benchmark performance vs baseline
- [ ] Verify commons-utils integration

**Success Criteria**:
- ✅ All components register correctly
- ✅ JSON schemas are correct and complete
- ✅ Validation integrates seamlessly
- ✅ Performance overhead <50ms
- ✅ No blocking bugs in Spring AI

**Deliverable**: Go/No-Go decision

---

### Phase 1: Library Restructuring (Weeks 2-3) 📦

**Goal**: Extract utilities to `sbb-mcp-commons-utils` v2.0.0

**Tasks**:
- [ ] Create new Maven module `sbb-mcp-commons-utils`
- [ ] Extract packages: validation, client, session, service, util, exception
- [ ] Update package names: `ch.sbb.mcp.commons` → `ch.sbb.mcp.commons.utils`
- [ ] Remove MCP protocol dependencies
- [ ] Migrate 80+ utility tests
- [ ] Verify 85%+ test coverage maintained
- [ ] Deploy v2.0.0 to GitHub Packages

**Deliverable**: Published `sbb-mcp-commons-utils:2.0.0`

---

### Phase 2: Tool Migration (Weeks 4-5) 🔧

**Goal**: Migrate swiss-mobility-mcp to Spring AI

**Tasks**:

**Week 4 - Read-only tools**:
- [ ] Migrate `GetBookingDetailsTool` → `@McpTool`
- [ ] Migrate `GetOfferDetailsTool` → `@McpTool`
- [ ] Migrate `GetRefundOptionsTool` → `@McpTool`
- [ ] Migrate `GetTicketPdfTool` → `@McpTool`
- [ ] Update tests for migrated tools

**Week 5 - State-modifying tools**:
- [ ] Migrate `GetTripPricingTool` → `@McpTool`
- [ ] Migrate `CreateBookingTool` → `@McpTool`
- [ ] Migrate `CancelBookingTool` → `@McpTool`
- [ ] Migrate `ProcessRefundTool` → `@McpTool`
- [ ] Update tests for migrated tools

**Deliverable**: All 8 tools migrated with passing tests

---

### Phase 3: Resources & Prompts (Week 6) 📚

**Goal**: Complete swiss-mobility-mcp migration

**Tasks**:
- [ ] Migrate 6 resources to `@McpResource`
- [ ] Migrate 4 prompts to `@McpPrompt`
- [ ] Update integration tests
- [ ] Update configuration (application.yml)
- [ ] Update pom.xml dependencies

**Deliverable**: swiss-mobility-mcp fully migrated

---

### Phase 4: Integration & Testing (Week 7) ✅

**Goal**: Validate production readiness

**Tasks**:
- [ ] End-to-end testing with Claude Desktop
- [ ] Performance benchmarking (vs baseline)
- [ ] Load testing (100+ concurrent requests)
- [ ] Redis session testing
- [ ] Progress tracking validation
- [ ] Error handling verification
- [ ] Documentation updates

**Success Criteria**:
- ✅ All 8 tools working
- ✅ All 6 resources working
- ✅ All 4 prompts working
- ✅ Performance within 10% of baseline
- ✅ 196+ tests passing

**Deliverable**: Production-ready swiss-mobility-mcp

---

### Phase 5: Journey Service Migration (Week 8) 🚆

**Goal**: Migrate second consumer project

**Tasks**:
- [ ] Migrate journey-service-mcp tools (~10 tools)
- [ ] Migrate resources
- [ ] Migrate prompts
- [ ] Update tests
- [ ] Integration testing

**Deliverable**: journey-service-mcp migrated

---

### Phase 6: Production Deployment (Week 9) 🚀

**Goal**: Deploy to production

**Tasks**:
- [ ] Blue-green deployment setup
- [ ] Deploy swiss-mobility-mcp staging
- [ ] Monitor for 48 hours
- [ ] Deploy swiss-mobility-mcp production
- [ ] Deploy journey-service-mcp staging
- [ ] Deploy journey-service-mcp production
- [ ] Monitor for 1 week
- [ ] Document lessons learned

**Deliverable**: Both servers in production with Spring AI

---

## Timeline

```
Week 1:    POC ──────────────────► Go/No-Go Decision
           │
Week 2-3:  Library Restructuring ──► commons-utils v2.0.0
           │
Week 4-5:  Tool Migration ──────────► 8 tools migrated
           │
Week 6:    Resources & Prompts ─────► swiss-mobility complete
           │
Week 7:    Integration Testing ─────► Production ready
           │
Week 8:    Journey Service ─────────► Both projects migrated
           │
Week 9:    Production Deployment ───► Live in production
```

**Total Duration**: 9 weeks (1 week POC + 8 weeks implementation)
**Team**: 2 developers at 50% capacity (~200 hours total)

---

## Risk Assessment

### High Priority Risks

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|------------|
| **Spring AI 2.0.0-M2 bugs** | Medium | Critical | POC validation first; pin version; rollback plan ready |
| **JSON schema generation issues** | Medium | High | Compare generated vs manual; add integration tests |
| **Session management complexity** | High | High | Dual-layer design (transport + business); extensive testing |
| **Performance regression** | Low | Medium | Benchmark before/after; <10% acceptable threshold |
| **Tool discovery failures** | Low | High | Integration tests for all tools; verify registration |
| **Breaking changes in Spring AI** | Medium | High | Pin to stable version; monitor releases; test upgrades |

### Rollback Plan

**Feature Toggle Approach**:
```xml
<profiles>
    <profile>
        <id>legacy</id>
        <!-- Rollback to sbb-mcp-commons v1.11.2 -->
    </profile>
    <profile>
        <id>spring-ai</id>
        <!-- Use Spring AI 2.0.0-M2 -->
    </profile>
</profiles>
```

**Quick Rollback**:
```bash
mvn clean install -P legacy
# Immediate rollback to previous stable version
```

---

## Success Metrics

### POC Phase
- [ ] All 4 POC components working
- [ ] JSON schemas validated
- [ ] Performance overhead <50ms
- [ ] No blocking bugs

### Implementation Phase
- [ ] 50% code reduction in tool definitions
- [ ] All 196+ tests passing
- [ ] Performance within 10% of baseline
- [ ] Zero production incidents during migration
- [ ] Documentation updated

### Long-term (6 months)
- [ ] Reduced maintenance time (target: -20%)
- [ ] Faster new tool development (target: -30% time)
- [ ] Developer satisfaction improved
- [ ] Zero schema/implementation mismatches

---

## Alternative Solutions Considered

### Alternative 1: Complete Replacement
**Approach**: Full migration to Spring AI, rebuild all infrastructure

**Pros**: Full Spring AI adoption, single dependency
**Cons**: 12-16 weeks effort, lose Resilience4j patterns, high risk
**Decision**: ❌ Rejected - Too risky, limited business value

### Alternative 2: Status Quo
**Approach**: Keep sbb-mcp-commons as-is

**Pros**: Zero risk, no migration cost
**Cons**: Ongoing maintenance burden, manual schemas, no Spring alignment
**Decision**: ⏸️ Fallback if POC fails

### Alternative 3: Gradual Migration
**Approach**: Add Spring AI alongside sbb-mcp-commons, migrate slowly

**Pros**: Very low risk, flexible timeline
**Cons**: Dual infrastructure, confusing for developers, prolonged migration
**Decision**: ❌ Rejected - Maintenance burden increased

---

## Dependencies

### Required Before Starting
- [ ] Spring AI 2.0.0-M2 released and stable
- [ ] Team capacity confirmed (2 developers × 50% for 8 weeks)
- [ ] POC budget approved (2 days)
- [ ] Architecture team sign-off

### Blocking Issues
- None currently identified

---

## Documentation

### Research Documents
- [Full Research Report](../docs/SPRING_AI_MIGRATION_RESEARCH.md) - Comprehensive analysis
- [Executive Summary](../docs/SPRING_AI_MIGRATION_EXECUTIVE_SUMMARY.md) - 1-page overview
- [Comparison Table](../docs/SPRING_AI_COMPARISON_TABLE.md) - Feature comparison
- [Documentation Index](../docs/SPRING_AI_MIGRATION_INDEX.md) - Navigation guide

### Implementation Guide
- Detailed plan: `~/.claude/plans/composed-wishing-lemur.md`
- Migration templates in research docs
- Testing strategies documented

---

## Questions for Discussion

### Technical
- [ ] Is Spring AI 2.0.0-M2 stable enough for production?
- [ ] Should we wait for Spring AI GA release?
- [ ] What's our performance regression tolerance? (Proposed: 10%)
- [ ] Redis session strategy confirmed?

### Process
- [ ] Who approves POC go/no-go decision?
- [ ] Do we need product manager sign-off?
- [ ] How do we communicate timeline to stakeholders?
- [ ] What's the rollback approval process?

### Resource
- [ ] Which 2 developers for 8 weeks at 50%?
- [ ] Do we need QA support for testing phase?
- [ ] DevOps support for deployment phase?

---

## Related Issues

- Closes #[old protocol maintenance issue if any]
- Related to #[tool boilerplate issue if any]
- Depends on: None
- Blocks: None

---

## Additional Context

### Current State
- **sbb-mcp-commons**: v1.11.2 (48 classes, 196 tests)
- **Consumers**: swiss-mobility-mcp (8 tools), journey-service-mcp (~10 tools)
- **Production**: 2 servers on Google Cloud Run
- **Stability**: High (battle-tested)

### Spring AI Context
- **Version**: 2.0.0-M2 (milestone release)
- **Status**: Under active development by Spring team
- **Community**: Growing, increasing adoption
- **Documentation**: Good and improving

---

## Approval Required From

- [ ] **Technical Lead**: Approach validation
- [ ] **Architecture Team**: Strategy approval
- [ ] **Product Manager**: Timeline impact
- [ ] **Engineering Manager**: Resource allocation

---

## Labels

- `enhancement`
- `migration`
- `spring-ai`
- `research-complete`
- `awaiting-decision`
- `high-priority`

---

## Assignees

- **Research**: @[researcher] ✅ Complete
- **POC**: TBD (pending decision)
- **Implementation**: TBD (pending POC)

---

**Created**: 2026-01-28
**Status**: 🔍 Research Complete - Awaiting Decision
**Next Action**: POC approval and resource assignment
