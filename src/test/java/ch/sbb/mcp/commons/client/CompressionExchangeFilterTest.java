package ch.sbb.mcp.commons.client;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link CompressionExchangeFilter}.
 */
class CompressionExchangeFilterTest {

    private MockWebServer mockWebServer;
    private WebClient webClient;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        webClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .filter(CompressionExchangeFilter.create())
                .build();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void shouldAddAcceptEncodingHeader() throws InterruptedException {
        // Given
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"message\":\"test\"}")
                .addHeader("Content-Type", "application/json"));

        // When
        webClient.get()
                .uri("/test")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // Then
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getHeader("Accept-Encoding")).isEqualTo("gzip");
    }

    @Test
    void shouldDecompressGzipResponse() throws IOException, InterruptedException {
        // Given
        String originalBody = "{\"message\":\"This is a test response that should be compressed\"}";
        byte[] compressedBody = gzip(originalBody);

        mockWebServer.enqueue(new MockResponse()
                .setBody(new okio.Buffer().write(compressedBody))
                .addHeader("Content-Type", "application/json")
                .addHeader("Content-Encoding", "gzip"));

        // When/Then
        StepVerifier.create(
                        webClient.get()
                                .uri("/test")
                                .retrieve()
                                .bodyToMono(String.class)
                )
                .expectNext(originalBody)
                .verifyComplete();
    }

    @Test
    void shouldHandleUncompressedResponse() throws InterruptedException {
        // Given
        String body = "{\"message\":\"uncompressed\"}";
        mockWebServer.enqueue(new MockResponse()
                .setBody(body)
                .addHeader("Content-Type", "application/json"));

        // When/Then
        StepVerifier.create(
                        webClient.get()
                                .uri("/test")
                                .retrieve()
                                .bodyToMono(String.class)
                )
                .expectNext(body)
                .verifyComplete();
    }

    @Test
    void shouldWorkWithPost() throws InterruptedException {
        // Given
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"id\":\"123\"}")
                .addHeader("Content-Type", "application/json"));

        // When
        webClient.post()
                .uri("/test")
                .bodyValue("{\"data\":\"test\"}")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // Then
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getHeader("Accept-Encoding")).isEqualTo("gzip");
        assertThat(request.getBody().readUtf8()).contains("data");
    }

    /**
     * Helper method to gzip compress a string.
     */
    private byte[] gzip(String data) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipStream = new GZIPOutputStream(byteStream)) {
            gzipStream.write(data.getBytes());
        }
        return byteStream.toByteArray();
    }
}
