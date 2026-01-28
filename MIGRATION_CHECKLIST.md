# Spring AI Migration - Implementation Checklist

**Status**: Research Complete
**Start Date**: TBD (pending approval)
**Target Completion**: TBD + 9 weeks

---

## Phase 0: POC (Week 1) 🔬

**Goal**: Validate Spring AI 2.0.0-M2 feasibility

### Planning
- [ ] Get POC approval from stakeholders
- [ ] Assign POC developer
- [ ] Set up POC project workspace

### Implementation (2 days)
- [ ] Create Spring Boot 3.5 project with Spring AI 2.0.0-M2
- [ ] Add sbb-mcp-commons-utils as dependency
- [ ] Implement simple tool: `add_numbers` (primitives)
- [ ] Implement complex tool: `poc__create_booking` (validation + progress)
- [ ] Implement resource: `poc_status`
- [ ] Implement prompt: `poc_workflow`

### Validation
- [ ] Verify all 4 components register
- [ ] Check JSON schemas are correct
- [ ] Test validation integration (commons-utils)
- [ ] Test progress tracking integration
- [ ] Benchmark performance (target: <50ms overhead)
- [ ] Test error handling

### Decision
- [ ] Document POC findings
- [ ] Present to architecture team
- [ ] **GO / NO-GO / DEFER decision made**
- [ ] If GO: Assign implementation team

---

## Phase 1: Library Restructuring (Weeks 2-3) 📦

**Goal**: Create `sbb-mcp-commons-utils` v2.0.0

### Week 2: Setup & Extract
- [ ] Create new Maven module: `sbb-mcp-commons-utils`
- [ ] Set up pom.xml with dependencies
- [ ] Extract `validation/` package
  - [ ] Validators.java
  - [ ] ValidationException.java
- [ ] Extract `client/` package
  - [ ] BaseApiClient.java
  - [ ] WebClientFactory.java
  - [ ] CompressionExchangeFilter.java
  - [ ] ApiClientException.java
- [ ] Extract `session/` package
  - [ ] McpSession.java
  - [ ] McpSessionStore.java
  - [ ] RedisMcpSessionStore.java
  - [ ] InMemoryMcpSessionStore.java
- [ ] Extract `service/` package
  - [ ] ProgressTracker.java
  - [ ] ProgressNotificationService.java
  - [ ] McpNotificationService.java
- [ ] Extract `util/` package
  - [ ] ArgumentExtractor.java
  - [ ] DateTimeUtil.java
  - [ ] GeoJsonValidator.java
- [ ] Extract `transformation/` package
  - [ ] ResponseTransformer.java
  - [ ] BaseResponseTransformer.java
- [ ] Extract `exception/` package
  - [ ] McpGlobalExceptionHandler.java
  - [ ] McpException.java

### Week 3: Testing & Publish
- [ ] Update package names: `ch.sbb.mcp.commons` → `ch.sbb.mcp.commons.utils`
- [ ] Remove MCP protocol dependencies
- [ ] Migrate tests:
  - [ ] ValidatorsTest (25 tests)
  - [ ] BaseApiClientTest (15 tests)
  - [ ] ProgressTrackerTest
  - [ ] RedisMcpSessionStoreTest (18 tests)
  - [ ] ArgumentExtractorTest (20 tests)
  - [ ] DateTimeUtilTest
  - [ ] Other utility tests
- [ ] Run full test suite: `mvn clean test`
- [ ] Verify coverage ≥85%
- [ ] Update README.md
- [ ] Tag version: `git tag v2.0.0`
- [ ] Deploy to GitHub Packages: `mvn deploy`
- [ ] Verify deployment successful

---

## Phase 2: Tool Migration - swiss-mobility-mcp (Weeks 4-5) 🔧

**Goal**: Migrate 8 tools to Spring AI

### Week 4: Read-Only Tools (4 tools)

#### GetBookingDetailsTool → @McpTool
- [ ] Create BookingTools class
- [ ] Migrate `getBookingDetails` method
- [ ] Add validation using commons-utils
- [ ] Update tests
- [ ] Verify JSON schema generated correctly

#### GetOfferDetailsTool → @McpTool
- [ ] Migrate `getOfferDetails` method
- [ ] Add validation
- [ ] Update tests
- [ ] Verify schema

#### GetRefundOptionsTool → @McpTool
- [ ] Migrate `getRefundOptions` method
- [ ] Add validation
- [ ] Update tests
- [ ] Verify schema

#### GetTicketPdfTool → @McpTool
- [ ] Migrate `getTicketPdf` method
- [ ] Add validation
- [ ] Update tests
- [ ] Verify schema

#### Week 4 Validation
- [ ] All 4 tools register with Spring AI
- [ ] All tests passing
- [ ] Manual testing with Claude Desktop

### Week 5: State-Modifying Tools (4 tools)

#### GetTripPricingTool → @McpTool
- [ ] Migrate `getTripPricing` method
- [ ] Add validation
- [ ] Update tests
- [ ] Verify schema

#### CreateBookingTool → @McpTool (Most Complex)
- [ ] Migrate `createBooking` method
- [ ] Integrate progress tracking (commons-utils)
- [ ] Extract session from context
- [ ] Add validation
- [ ] Update tests
- [ ] Verify schema
- [ ] Test progress notifications

#### CancelBookingTool → @McpTool
- [ ] Migrate `cancelBooking` method
- [ ] Add validation
- [ ] Update tests
- [ ] Verify schema

#### ProcessRefundTool → @McpTool
- [ ] Migrate `processRefund` method
- [ ] Add validation
- [ ] Update tests
- [ ] Verify schema

#### Week 5 Validation
- [ ] All 8 tools migrated
- [ ] All tests passing (196+ tests)
- [ ] Progress tracking works
- [ ] Session management works
- [ ] Manual E2E testing

---

## Phase 3: Resources & Prompts (Week 6) 📚

**Goal**: Complete swiss-mobility-mcp migration

### Resources (Day 1-3)

#### Create SwissMobilityResources class
- [ ] Migrate ServiceStatusResource → `@McpResource getServiceStatus()`
- [ ] Migrate NetworksResource → `@McpResource getNetworks()`
- [ ] Migrate FareTypesResource → `@McpResource getFareTypes()`
- [ ] Migrate TravelClassesResource → `@McpResource getTravelClasses()`
- [ ] Migrate ProductsResource → `@McpResource getProducts()`
- [ ] Migrate ServiceInfoResource → `@McpResource getServiceInfo()`

#### Validation
- [ ] All 6 resources register
- [ ] Resource read operations work
- [ ] Update tests

### Prompts (Day 4-5)

#### Create SwissMobilityPrompts class
- [ ] Migrate `price-and-book-trip` → `@McpPrompt`
- [ ] Migrate `manage-existing-booking` → `@McpPrompt`
- [ ] Migrate `compare-fare-options` → `@McpPrompt`
- [ ] Migrate `payment-safety-reminder` → `@McpPrompt`

#### Validation
- [ ] All 4 prompts register
- [ ] Prompt templates render correctly
- [ ] Update tests

### Configuration & Cleanup
- [ ] Update `pom.xml`:
  - [ ] Add Spring AI dependency
  - [ ] Add commons-utils v2.0.0
  - [ ] Remove old sbb-mcp-commons
- [ ] Update `application.yml` for Spring AI
- [ ] Remove old tool/resource/prompt classes
- [ ] Update imports across codebase
- [ ] Run full test suite
- [ ] Fix any broken tests

---

## Phase 4: Integration & Testing (Week 7) ✅

**Goal**: Validate production readiness

### Integration Testing (Day 1-2)
- [ ] Test all 8 tools via Claude Desktop
- [ ] Test all 6 resources
- [ ] Test all 4 prompts
- [ ] Test progress tracking in long operations
- [ ] Test Redis session persistence
- [ ] Test error handling paths
- [ ] Test validation error messages

### Performance Testing (Day 3)
- [ ] Baseline current performance
- [ ] Benchmark migrated version
- [ ] Compare results
- [ ] Tool invocation latency: <10% regression
- [ ] Memory usage: <15% increase
- [ ] Startup time: <200ms increase

### Load Testing (Day 4)
- [ ] 100 concurrent requests sustained
- [ ] 1000 requests total test
- [ ] Monitor error rates
- [ ] Monitor response times
- [ ] Monitor Redis connection pool

### Documentation (Day 5)
- [ ] Update README.md
- [ ] Update CLAUDE.md
- [ ] Update API documentation
- [ ] Document new dependency structure
- [ ] Migration guide for other consumers

---

## Phase 5: Journey Service Migration (Week 8) 🚆

**Goal**: Migrate second consumer project

### Setup
- [ ] Update journey-service-mcp pom.xml
- [ ] Add Spring AI + commons-utils dependencies
- [ ] Remove old sbb-mcp-commons

### Tool Migration (~10 tools)
- [ ] List all tools to migrate
- [ ] Migrate each tool to @McpTool
- [ ] Update tests for each tool
- [ ] Verify schemas

### Resources & Prompts
- [ ] Migrate resources
- [ ] Migrate prompts
- [ ] Update tests

### Testing
- [ ] Run full test suite
- [ ] Integration testing
- [ ] Manual testing with Claude Desktop

---

## Phase 6: Production Deployment (Week 9) 🚀

**Goal**: Deploy to production with monitoring

### Pre-Deployment
- [ ] Code review complete
- [ ] All tests passing (swiss-mobility + journey-service)
- [ ] Performance benchmarks met
- [ ] Documentation updated
- [ ] Rollback plan tested
- [ ] Monitoring dashboards updated
- [ ] Health checks configured

### Staging Deployment (Day 1-2)

#### swiss-mobility-mcp
- [ ] Deploy to staging environment
- [ ] Smoke tests
- [ ] Monitor for 24 hours
- [ ] Check error rates, latency, memory

#### journey-service-mcp
- [ ] Deploy to staging
- [ ] Smoke tests
- [ ] Monitor for 24 hours
- [ ] Check metrics

### Production Deployment (Day 3-5)

#### swiss-mobility-mcp
- [ ] Blue-green deployment
- [ ] Route 10% traffic to new version
- [ ] Monitor for 4 hours
- [ ] Route 50% traffic
- [ ] Monitor for 4 hours
- [ ] Route 100% traffic
- [ ] Keep old version for 1 week

#### journey-service-mcp
- [ ] Blue-green deployment
- [ ] Gradual traffic increase
- [ ] Full cutover
- [ ] Monitor

### Post-Deployment (Day 6-7)
- [ ] Monitor for 48 hours
- [ ] Check all metrics normal
- [ ] No error rate increase
- [ ] No performance degradation
- [ ] Verify Redis sessions working
- [ ] Verify progress tracking working

### Cleanup
- [ ] Remove old sbb-mcp-commons v1.11.2 code
- [ ] Archive POC project
- [ ] Update internal wikis
- [ ] Announce to team
- [ ] Close migration issue

---

## Rollback Procedures

### Immediate Rollback (if issues found)
- [ ] Switch Maven profile: `mvn -P legacy`
- [ ] Redeploy previous version
- [ ] Document issues found
- [ ] Create bug tickets

### Partial Rollback (one service)
- [ ] Identify affected service
- [ ] Rollback that service only
- [ ] Keep other service on new version
- [ ] Investigate root cause

---

## Success Criteria

### Technical
- [ ] All tools migrated and working
- [ ] All resources migrated and working
- [ ] All prompts migrated and working
- [ ] 196+ tests passing
- [ ] Performance within 10% of baseline
- [ ] No production incidents

### Process
- [ ] Zero unplanned rollbacks
- [ ] All documentation updated
- [ ] Team trained on new patterns
- [ ] Lessons learned documented

### Business
- [ ] Reduced tool development time (measure after 1 month)
- [ ] Reduced maintenance burden (measure after 3 months)
- [ ] Developer satisfaction improved

---

## Weekly Status Updates

### Week 1: POC
**Status**:
**Blockers**:
**Next Week**:

### Week 2-3: Library
**Status**:
**Blockers**:
**Next Week**:

### Week 4-5: Tools
**Status**:
**Blockers**:
**Next Week**:

### Week 6: Resources/Prompts
**Status**:
**Blockers**:
**Next Week**:

### Week 7: Testing
**Status**:
**Blockers**:
**Next Week**:

### Week 8: Journey Service
**Status**:
**Blockers**:
**Next Week**:

### Week 9: Production
**Status**:
**Blockers**:
**Next Week**:

---

## Contacts & Resources

**Team**:
- POC Developer: TBD
- Implementation Team: TBD (2 developers)
- Tech Lead: TBD
- QA Support: TBD
- DevOps: TBD

**Communication**:
- Slack: #mcp-migration
- Weekly sync: TBD
- Status updates: TBD

**Links**:
- Research docs: `/docs/SPRING_AI_MIGRATION_*`
- GitHub issue: TBD
- Project board: TBD

---

**Last Updated**: 2026-01-28
**Overall Status**: ⏳ Awaiting POC Approval
