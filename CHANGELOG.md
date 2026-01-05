# Changelog

All notable changes to this project will be documented in this file.

## [1.0.0-SNAPSHOT] - 2026-01-05

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

### Summary
- 13 reusable components
- ~1,407 lines of code
- 80-85% code reusability achieved
- Supports journey-service-mcp and swiss-mobility-mcp
