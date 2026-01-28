# Spring AI Migration - Executive Summary

**Date**: January 28, 2026
**Status**: Research Complete - Awaiting Decision
**Document**: 1-page stakeholder summary

---

## The Question

Should we migrate from our custom **sbb-mcp-commons** library to **Spring AI 2.0.0-M2**?

---

## The Recommendation

**YES** - with a **Hybrid Approach**:
- Migrate MCP protocol handling → Spring AI
- Preserve infrastructure utilities → New `sbb-mcp-commons-utils` library

---

## Why Migrate?

### Current Pain Points
- **Maintenance Burden**: We maintain MCP protocol implementation ourselves
- **Boilerplate Code**: Manual JSON schema definitions for every tool
- **Protocol Updates**: Must track and implement MCP spec changes

### Spring AI Benefits
- ✅ **50% less boilerplate**: Auto-generated JSON schemas from `@McpTool` annotations
- ✅ **Official support**: Spring team maintains protocol updates
- ✅ **Modern approach**: Annotation-based, consistent with Spring ecosystem
- ✅ **Built-in transports**: STDIO, SSE, HTTP out-of-the-box

### What We Keep (80% of our code)
- ✅ **Validation framework**: 15+ validation methods, heavily used
- ✅ **API client utilities**: Resilience4j patterns (circuit breaker, retry)
- ✅ **Redis sessions**: Business state persistence
- ✅ **Progress tracking**: Multi-step operation infrastructure

---

## The Numbers

| Metric | Current | After Migration |
|--------|---------|-----------------|
| **Dependencies** | 1 (sbb-mcp-commons) | 2 (Spring AI + utils) |
| **Tool Definition LOC** | ~50 lines/tool | ~20 lines/tool |
| **JSON Schema** | Manual (error-prone) | Auto-generated |
| **Maintenance** | Full protocol | Utilities only |
| **Risk Level** | N/A | Medium (mitigated) |

---

## Timeline & Effort

### Phase 0: POC (Week 1)
- **Duration**: 2 days
- **Team**: 1 developer
- **Output**: Go/No-Go decision

### Full Migration (If GO)
- **Duration**: 6-8 weeks
- **Team**: 2 developers at 50% time
- **Projects**: swiss-mobility-mcp, journey-service-mcp

### Phases
1. Library restructuring (2 weeks)
2. Tool migration (2 weeks)
3. Resources & prompts (1 week)
4. Testing & rollout (2-3 weeks)

---

## Risk Assessment

| Risk | Level | Mitigation |
|------|-------|------------|
| Spring AI bugs | Medium | POC validation first; rollback plan |
| Performance regression | Low | Benchmark; <10% acceptable |
| Session complexity | High | Dual-layer design (transport + business) |
| Migration disruption | Medium | 6-8 week timeline; phased rollout |

**Rollback Plan**: Feature toggle for immediate rollback to v1.11.2

---

## Cost-Benefit

### Costs
- **One-time**: 6-8 weeks migration effort (~200 hours)
- **Ongoing**: Maintain utils library (~20% of current effort)

### Benefits
- **Immediate**: 50% less boilerplate, simpler development
- **Ongoing**: Reduced maintenance (Spring team handles protocol)
- **Strategic**: Alignment with official Spring ecosystem

### ROI Estimate
- **Break-even**: 6 months (time saved on maintenance)
- **Long-term**: Significant reduction in protocol maintenance

---

## Decision Framework

### ✅ GO if...
- POC validates functionality
- Performance acceptable (<10% regression)
- Team has 6-8 weeks capacity
- Spring AI stable (no blockers)

### ❌ NO-GO if...
- POC reveals blocking issues
- Performance regression >20%
- Timeline conflicts with priorities
- Spring AI too immature

### ⏸️ DEFER if...
- Spring AI not yet GA
- Need more validation
- Team capacity constraints

---

## Recommendation

**Proceed with POC** (2 days, 1 developer)

**Why POC First**:
- Validates Spring AI 2.0.0-M2 works with our stack
- Tests JSON schema quality
- Confirms commons-utils integration
- Minimal investment for high confidence

**After POC**:
- ✅ GO → 6-8 week migration
- ❌ NO-GO → Stay with sbb-mcp-commons
- ⏸️ DEFER → Re-evaluate in 6 months

---

## Next Steps

### This Week
1. **Review** this research with architecture team
2. **Decide** whether to proceed with POC
3. **Assign** developer for POC (if GO)

### Next Week (If GO)
1. **Implement** POC (2 days)
2. **Review** POC results (1 day)
3. **Decide** on full migration

### Weeks 3-10 (If Full GO)
1. **Execute** migration phases
2. **Test** thoroughly
3. **Deploy** to production

---

## Key Contacts

- **Technical Lead**: [Responsible for POC execution]
- **Architecture Team**: [Final decision authority]
- **Stakeholders**: [Informed of timeline impact]

---

## Questions?

See full research document: `docs/SPRING_AI_MIGRATION_RESEARCH.md`

Or contact: [Your name/team]

---

**Bottom Line**: Spring AI offers real benefits with manageable risk. POC validation is the smart next step.
