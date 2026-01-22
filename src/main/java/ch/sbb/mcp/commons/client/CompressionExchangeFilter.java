package ch.sbb.mcp.commons.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

/**
 * WebClient exchange filter that enables HTTP compression via content negotiation.
 *
 * <p><strong>Content Negotiation (RFC 7231):</strong></p>
 * <ul>
 *   <li><strong>Accept-Encoding: gzip</strong> - Client signals it can accept compressed responses</li>
 *   <li><strong>Content-Encoding: gzip</strong> - Server indicates response is compressed</li>
 *   <li><strong>Automatic Decompression:</strong> Reactor Netty handles response decompression transparently</li>
 * </ul>
 *
 * <p><strong>How It Works:</strong></p>
 * <pre>
 * 1. Client sends request with: Accept-Encoding: gzip
 * 2. Server compresses response and sends: Content-Encoding: gzip
 * 3. Reactor Netty auto-decompresses response
 * 4. Application receives uncompressed data
 * </pre>
 *
 * <p><strong>Request Body Compression:</strong></p>
 * <p>Spring WebFlux doesn't provide built-in request body compression via filters.
 * For request compression, use Reactor Netty's HttpClient configuration directly:</p>
 * <pre>{@code
 * HttpClient httpClient = HttpClient.create()
 *     .compress(true); // Enables request compression
 *
 * WebClient client = WebClient.builder()
 *     .clientConnector(new ReactorClientHttpConnector(httpClient))
 *     .filter(CompressionExchangeFilter.create())
 *     .build();
 * }</pre>
 *
 * <p><strong>Usage (Response Compression Only):</strong>
 * <pre>{@code
 * WebClient client = WebClient.builder()
 *     .filter(CompressionExchangeFilter.create())
 *     .build();
 * }</pre>
 *
 * <p><strong>Benefits:</strong>
 * <ul>
 *   <li><strong>Bandwidth Reduction:</strong> 70-80% for JSON responses</li>
 *   <li><strong>Performance:</strong> Lower latency on slower connections</li>
 *   <li><strong>Cost Savings:</strong> Reduced network ingress charges</li>
 *   <li><strong>Standards Compliant:</strong> RFC 7231 HTTP content negotiation</li>
 *   <li><strong>Server Compatibility:</strong> Works with all HTTP/1.1+ servers</li>
 * </ul>
 *
 * <p><strong>Note:</strong> This filter only handles response compression via Accept-Encoding.
 * For bidirectional compression (requests + responses), use Reactor Netty's {@code compress(true)}
 * configuration along with this filter.
 */
public class CompressionExchangeFilter implements ExchangeFilterFunction {

    private static final Logger log = LoggerFactory.getLogger(CompressionExchangeFilter.class);
    private static final String GZIP = "gzip";

    /**
     * Creates a new compression filter instance.
     * Adds Accept-Encoding: gzip header to requests.
     *
     * @return new CompressionExchangeFilter
     */
    public static CompressionExchangeFilter create() {
        return new CompressionExchangeFilter();
    }

    private CompressionExchangeFilter() {
        log.debug("CompressionExchangeFilter initialized (response compression via Accept-Encoding)");
    }

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        // Add Accept-Encoding: gzip header to request compressed responses
        // Reactor Netty automatically decompresses gzip responses
        ClientRequest compressedRequest = ClientRequest.from(request)
                .header(HttpHeaders.ACCEPT_ENCODING, GZIP)
                .build();

        log.trace("Added Accept-Encoding: gzip to request: {} {}",
                request.method(), request.url());

        return next.exchange(compressedRequest)
                .doOnSuccess(response -> {
                    String contentEncoding = response.headers().asHttpHeaders()
                            .getFirst(HttpHeaders.CONTENT_ENCODING);

                    if (GZIP.equalsIgnoreCase(contentEncoding)) {
                        log.debug("Received gzip response from {} (auto-decompressed by Reactor Netty)",
                                request.url());
                    }
                });
    }
}
