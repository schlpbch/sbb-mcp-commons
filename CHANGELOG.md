# Changelog

All notable changes to this project will be documented in this file.

## [1.9.0] - 2026-01-13

### 📊 Strategic Analysis

**Spring AI MCP Server Evaluation:**

- Conducted comprehensive evaluation of Spring AI MCP server as potential replacement
- Documented 10 critical gaps in Spring AI compared to sbb-mcp-commons
- **Decision: Continue with sbb-mcp-commons** for production services

**Key Findings:**

- ✅ sbb-mcp-commons provides production-grade infrastructure Spring AI lacks:
  - Distributed session management with Redis + circuit breakers
  - Resilience4j integration (retry, circuit breaker, rate limiting)
  - Comprehensive validation framework
  - Reactive API client infrastructure with retry patterns
  - GeoJSON/JTS geometry support
  - Request context management (correlation IDs, session tracking)
  - Battle-tested Cloud Run deployment patterns

- ⚠️ Migration to Spring AI would require:
  - 8-12 weeks of development effort
  - Rebuilding ~7,000+ lines of infrastructure code
  - High risk to production stability
  - Affecting 500+ files across consuming projects

**Documentation Added:**

- `docs/SPRING_AI_EVALUATION.md` - Executive summary and strategic recommendation
- `docs/SPRING_AI_GAP_ANALYSIS.md` - Detailed technical gap analysis with code examples

**Recommendation:**

- Keep sbb-mcp-commons for existing production servers (journey-service-mcp, swiss-mobility-mcp)
- Monitor Spring AI evolution for maturity improvements
- Re-evaluate in Q3 2026 when Spring AI reaches v2.0+
- Consider Spring AI for new MCP servers in 2026 H2

### 📚 References

- [Spring AI MCP Evaluation](docs/SPRING_AI_EVALUATION.md)
- [Spring AI Gap Analysis](docs/SPRING_AI_GAP_ANALYSIS.md)

---

## [1.8.0] - 2026-01-06

### 🧪 Test Coverage Improvements

#### Phase 1: High-Priority Infrastructure Components

- **New Test Classes** (50+ test methods, 100+ assertions)
  - `McpToolRegistryTest` (8 tests) - Tool discovery and invocation
  - `BaseToolHandlerTest` (20+ tests) - Handler execution and argument extraction
  - `SimpleRateLimiterTest` (15+ tests) - Rate limiting and thread safety
  - `McpResourceTest` (9 tests) - Resource interface and URI generation

### 📊 Coverage Improvements

| Component           | Before | After |
| ------------------- | ------ | ----- |
| `McpToolRegistry`   | 0%     | ~90%  |
| `BaseToolHandler`   | 0%     | ~85%  |
| `SimpleRateLimiter` | 0%     | ~90%  |
| `McpResource`       | 0%     | ~80%  |

### 🔧 Technical Changes

- **Java Version**: Migrated from Java 25 → Java 21 (LTS)
  - Ensures Mockito/ByteBuddy compatibility
  - Provides stable testing infrastructure
  - Aligns with production deployment standards

### ✅ Test Results

- **309 tests passing** (0 failures, 2 skipped)
- **Thread safety verified** for concurrent rate limiting
- **Reactive testing** with StepVerifier
- **Comprehensive edge cases** covered

### 📝 Test Highlights

- Auto-discovery of Spring beans (McpToolRegistry)
- Argument extraction with type coercion (BaseToolHandler)
- Concurrent request handling (SimpleRateLimiter)
- URI generation and normalization (McpResource)
- Error propagation and validation testing

### 📚 Documentation

- Comprehensive implementation plan
- Detailed walkthrough with test descriptions
- Task breakdown for future phases

---

## [1.7.0] - 2026-01-05

### 🔒 Security Hardening

**CRITICAL Fixes:**

- **Fixed Jackson deserialization RCE vulnerability** (CVE-2017-7525, CVSS 10.0)
  - Removed unsafe `@JsonTypeInfo` annotations from `McpSession`
  - Prevents remote code execution through polymorphic deserialization

**HIGH Severity Fixes:**

- **SSRF Protection** (CVSS 8.6)
  - Added comprehensive URL validation in `WebClientFactory`
  - Blocks private IP ranges, localhost, and link-local addresses
  - Validates URL schemes (allows only http/https)
- **Session ID Validation** (CVSS 7.5)
  - Implemented strict session ID format validation in `RedisMcpSessionStore`
  - Prevents Redis command injection attacks

**MEDIUM Severity Fixes:**

- **Input Sanitization**
  - Added length limits and null byte detection in `ArgumentExtractor`
  - Prevents buffer overflow and injection attacks
- **Error Message Sanitization**
  - Enhanced `McpGlobalExceptionHandler` to prevent information disclosure
  - Sanitizes stack traces and sensitive error details
- **Rate Limiting Infrastructure**
  - New `SimpleRateLimiter` component for DoS protection
  - Token bucket algorithm with configurable limits
- **Secure TLS Configuration**
  - `WebClientFactory` enforces TLS 1.2/1.3 only
  - Disables insecure protocols and cipher suites

**LOW Severity Fixes:**

- Improved email validation regex in `Validators`
- Security audit of logging to prevent sensitive data exposure

### 📊 Statistics

- **10 vulnerabilities fixed** (1 critical, 2 high, 5 medium, 2 low)
- **11 new security tests** added
- **~580 lines** of security improvements
- **207/207 tests passing** (100% success rate)

### 📚 Documentation

- Added comprehensive architecture documentation
- New guides for prompts, sessions, and quick start
- Enhanced README with usage examples and diagrams

### ✅ Production Status

Ready for production deployment with comprehensive security hardening.

---

## [1.6.1] - 2026-01-05

### Added

- Auto-configuration for MCP prompt infrastructure
- Comprehensive tests for prompt handling

---

## [1.6.0] - 2026-01-05

### Added

- MCP Prompt Infrastructure
- Auto-discovery and registration of prompts
- `McpPromptHandler` for `prompts/list` and `prompts/get`

---

## [1.5.0] - 2026-01-05

### Added

- Initial MCP Prompt Infrastructure implementation

---

## [1.4.0] - 2026-01-05

### Added

**Phase 1: Foundation**

- Validation framework (`Validators`, `ValidationException`)
- Response transformation (`ResponseTransformer`, `BaseResponseTransformer`)
- API models (`BaseApiRequest`, `BaseApiResponse`, `ApiError`)

**Phase 2: Handlers and Services**

- Tool handler framework (`BaseToolHandler`, `ToolResult`)
- Service layer abstraction (`BaseService`)

**Phase 3: API Client**

- Generic API client (`BaseApiClient`)
- WebClient factory (`WebClientFactory`)
- API client exception (`ApiClientException`)

**Session Management**

- Reactive session management with `McpSession` and `McpSessionStore`
- In-memory and Redis implementations
- Auto-configuration support

### Summary

- 13+ reusable components
- ~1,407 lines of code
- 80-85% code reusability achieved
- Supports journey-service-mcp and swiss-mobility-mcp
- Java 25 support
- 50+ comprehensive tests
