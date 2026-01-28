# Copy this to create GitHub Issue

---

**Title**: `[Feature Request] Migrate to Spring AI MCP Server`

**Labels**: `enhancement`, `migration`, `spring-ai`, `research-complete`, `high-priority`

---

## 🎯 Summary

Migrate sbb-mcp-commons to Spring AI 2.0.0-M2 using **hybrid approach**:
- Migrate MCP protocol → Spring AI
- Preserve infrastructure → `sbb-mcp-commons-utils` v2.0.0

---

## 💡 Why?

### Problems Today
- 🔧 **Maintenance burden**: We maintain entire MCP protocol
- 📝 **50 lines boilerplate** per tool with manual JSON schemas
- ❌ **Error-prone**: Manual schemas drift from implementation
- 🔄 **Protocol updates**: Must track MCP spec changes manually

### Benefits After Migration
- ✅ **50% less code**: ~20 lines/tool with auto-generated schemas
- ✅ **Less maintenance**: Spring team handles protocol updates
- ✅ **Type safety**: No more `Map<String, Object>`
- ✅ **Preserve 80%**: Keep validation, API client, Redis sessions

---

## 📋 Before/After Example

### Current (sbb-mcp-commons)
```java
@Component
public class CreateBookingTool extends BaseMcpTool<Input, JsonNode> {
    public record Input(...) {}

    @Override
    protected Input validateAndParse(Map<String, Object> args) {
        // Manual casting, validation
    }

    @Override
    public String inputSchema() {
        return """{"type": "object", ...}""";  // Manual, error-prone
    }
}
```
**Lines**: ~50

### After (Spring AI + commons-utils)
```java
@Component
public class BookingTools {
    @McpTool(name = "create_booking", ...)
    public Mono<JsonNode> createBooking(
        @McpToolParam(required = true) String offerId,  // Type-safe
        @McpToolParam(required = true) List<Passengers> passengers
    ) {
        Validators.requireNonEmpty(offerId);  // Still use commons-utils
        return bookingService.create(offerId, passengers);
    }
    // Schema auto-generated from method signature ✅
}
```
**Lines**: ~20

---

## 🗺️ Implementation Plan

### Phase 0: POC (Week 1) 🔬
**Goal**: Validate Spring AI works

**Tasks**:
- [ ] Create POC with 2 tools, 1 resource, 1 prompt
- [ ] Verify JSON schema generation
- [ ] Test commons-utils integration
- [ ] Benchmark performance

**Deliverable**: Go/No-Go decision

---

### Phase 1: Library (Weeks 2-3) 📦
- [ ] Create `sbb-mcp-commons-utils` v2.0.0
- [ ] Extract validation, client, session, utils
- [ ] Deploy to GitHub Packages

---

### Phase 2: Tools (Weeks 4-5) 🔧
- [ ] Migrate 8 tools in swiss-mobility-mcp
- [ ] Update all tests

---

### Phase 3: Resources & Prompts (Week 6) 📚
- [ ] Migrate 6 resources + 4 prompts
- [ ] Integration testing

---

### Phase 4: Testing (Week 7) ✅
- [ ] End-to-end with Claude Desktop
- [ ] Performance benchmarking
- [ ] Load testing

---

### Phase 5: Journey Service (Week 8) 🚆
- [ ] Migrate journey-service-mcp (~10 tools)

---

### Phase 6: Production (Week 9) 🚀
- [ ] Blue-green deployment
- [ ] Monitor for 1 week

---

## ⏱️ Timeline

**Total**: 9 weeks (1 POC + 8 implementation)
**Team**: 2 developers at 50% (~200 hours)

```
Week 1:    POC → Decision
Week 2-3:  commons-utils v2.0.0
Week 4-5:  Tool migration
Week 6:    Resources & prompts
Week 7:    Testing
Week 8:    Journey service
Week 9:    Production
```

---

## ⚠️ Risks & Mitigation

| Risk | Mitigation |
|------|------------|
| Spring AI bugs | POC validates first; rollback plan |
| Performance | <10% acceptable; benchmark |
| Session complexity | Dual-layer (transport + business) |

**Rollback**: Feature toggle for immediate revert to v1.11.2

---

## 📊 Success Metrics

### POC
- [ ] All components working
- [ ] Performance <50ms overhead
- [ ] No blocking bugs

### Implementation
- [ ] 50% code reduction
- [ ] 196+ tests passing
- [ ] Performance within 10%

---

## 📚 Documentation

**Research Complete**:
- [Full Report](docs/SPRING_AI_MIGRATION_RESEARCH.md) - Comprehensive
- [Executive Summary](docs/SPRING_AI_MIGRATION_EXECUTIVE_SUMMARY.md) - 1 page
- [Comparison Table](docs/SPRING_AI_COMPARISON_TABLE.md) - Features
- [Index](docs/SPRING_AI_MIGRATION_INDEX.md) - Navigation

---

## ✋ Decision Points

### Go/No-Go Criteria

**GO if**:
- ✅ POC validates functionality
- ✅ Performance acceptable
- ✅ Team has capacity

**NO-GO if**:
- ❌ Spring AI has blockers
- ❌ Performance regression >20%
- ❌ Can't integrate commons-utils

---

## 👥 Approval Needed

- [ ] Technical Lead - Approach
- [ ] Architecture Team - Strategy
- [ ] Product Manager - Timeline
- [ ] Engineering Manager - Resources

---

## 🔗 Related

- Research: See docs/ folder
- Consumers: swiss-mobility-mcp, journey-service-mcp
- Spring AI: https://docs.spring.io/spring-ai/reference/

---

## 💬 Discussion

**Questions**:
1. Is Spring AI 2.0.0-M2 stable for production?
2. Should we wait for GA?
3. Which 2 developers for 8 weeks?

**Next Step**: Team review & POC approval

---

**Status**: 🔍 Research Complete - Awaiting Decision
**Created**: 2026-01-28
