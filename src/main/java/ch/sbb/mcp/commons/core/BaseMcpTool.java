package ch.sbb.mcp.commons.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Abstract base class for MCP tools providing structured validation and error
 * handling.
 *
 * <p>
 * This template implements a standardized tool invocation pattern combining:
 * </p>
 * <ul>
 * <li><b>Structured validation</b>: Parse and validate input parameters before
 * execution</li>
 * <li><b>Type safety</b>: Strongly-typed input (I) and output (O)
 * parameters</li>
 * <li><b>Standard error handling</b>: Consistent error conversion to
 * McpResult</li>
 * <li><b>State modification flag</b>: Override isStateModifying() for write
 * operations</li>
 * </ul>
 *
 * <p>
 * <b>Usage Example:</b>
 * </p>
 * 
 * <pre>
 * {
 *   &#64;code
 *   public class SearchStationsTool extends BaseMcpTool<SearchInput, List<Station>> {
 *
 *     &#64;Override
 *     protected SearchInput validateAndParse(Map<String, Object> args) {
 *       String query = ArgumentExtractor.getRequiredString(args, "query");
 *       int limit = ArgumentExtractor.getOptionalInt(args, "limit").orElse(10);
 *       return new SearchInput(query, limit);
 *     }
 *
 *     &#64;Override
 *     protected Mono<List<Station>> executeInternal(SearchInput input) {
 *       return stationService.search(input.query(), input.limit());
 *     }
 *
 *     @Override
 *     public boolean isStateModifying() {
 *       return false; // read-only operation
 *     }
 *
 *     // ... implement name(), summary(), description(), inputSchema()
 *   }
 * }
 * </pre>
 *
 * <p>
 * <b>Error Handling:</b>
 * </p>
 * <ul>
 * <li>{@link IllegalArgumentException} →
 * {@link McpError.ErrorCode#INVALID_INPUT}</li>
 * <li>Other exceptions → {@link McpError.ErrorCode#INTERNAL_ERROR}</li>
 * </ul>
 *
 * @param <I> the input parameter type (validated)
 * @param <O> the output result type (before wrapping in McpResult)
 * @see McpTool
 * @see McpResult
 * @see McpError
 */
public abstract class BaseMcpTool<I, O> implements McpTool<McpResult<O>> {

  private static final Logger log = LoggerFactory.getLogger(BaseMcpTool.class);

  /**
   * Validates and parses raw tool arguments into a strongly-typed input object.
   *
   * <p>
   * This method should:
   * </p>
   * <ul>
   * <li>Extract required and optional parameters from the arguments map</li>
   * <li>Validate parameter types, ranges, and business rules</li>
   * <li>Throw {@link IllegalArgumentException} for validation failures</li>
   * </ul>
   *
   * @param args raw tool arguments as provided by the MCP client
   * @return validated and parsed input object
   * @throws IllegalArgumentException if validation fails (converted to
   *                                  INVALID_INPUT error)
   */
  protected abstract I validateAndParse(Map<String, Object> args);

  /**
   * Executes the tool's core logic with validated input.
   *
   * <p>
   * This method contains the business logic and should:
   * </p>
   * <ul>
   * <li>Perform the tool's primary operation</li>
   * <li>Return a Mono containing the result</li>
   * <li>Allow exceptions to propagate (they will be handled by the template)</li>
   * </ul>
   *
   * @param input validated input parameters
   * @return a Mono containing the operation result
   */
  protected abstract Mono<O> executeInternal(I input);

  /**
   * Template method implementing the standard tool invocation pattern.
   *
   * <p>
   * This final method coordinates validation, execution, and error handling:
   * </p>
   * <ol>
   * <li>Validate and parse input arguments → {@link #validateAndParse(Map)}</li>
   * <li>Execute tool logic → {@link #executeInternal(Object)}</li>
   * <li>Wrap result in {@link McpResult.Success}</li>
   * <li>Handle errors → {@link #handleError(Throwable)}</li>
   * </ol>
   *
   * @param args raw tool arguments
   * @return a Mono containing McpResult with either success data or error
   */
  @Override
  public final Mono<McpResult<O>> invoke(Map<String, Object> args) {
    return Mono.fromCallable(() -> validateAndParse(args))
        .flatMap(this::executeInternal)
        .map(McpResult::success)
        .onErrorResume(this::handleError);
  }

  /**
   * Handles errors by converting them to appropriate McpResult failures.
   *
   * <p>
   * Default error mapping:
   * </p>
   * <ul>
   * <li>{@link IllegalArgumentException} → INVALID_INPUT (not retryable)</li>
   * <li>Other exceptions → INTERNAL_ERROR (not retryable)</li>
   * </ul>
   *
   * <p>
   * Override this method to customize error handling for specific exception
   * types.
   * </p>
   *
   * @param throwable the exception that occurred
   * @return a Mono containing an McpResult failure
   */
  protected Mono<McpResult<O>> handleError(Throwable throwable) {
    log.error("Error executing tool {}: {}", name(), throwable.getMessage(), throwable);

    if (throwable instanceof IllegalArgumentException) {
      return Mono.just(McpResult.invalidInput(throwable.getMessage(), null));
    }

    return Mono.just(McpResult.failure(
        new McpError(
            McpError.ErrorCode.INTERNAL_ERROR,
            "Tool execution failed: " + throwable.getMessage(),
            throwable.getClass().getSimpleName(),
            false)));
  }

  /**
   * Returns whether this tool modifies state.
   *
   * <p>
   * Override this method to return {@code true} for state-modifying operations
   * (create, update, delete, booking, payment, etc.).
   * </p>
   *
   * @return false by default (read-only operation)
   */
  @Override
  public boolean isStateModifying() {
    return false;
  }

  /**
   * Returns the tool's category for grouping in the UI.
   *
   * <p>
   * Override to provide a meaningful category (e.g., "search", "booking",
   * "information").
   * </p>
   *
   * @return "general" by default
   */
  @Override
  public String category() {
    return "general";
  }
}
