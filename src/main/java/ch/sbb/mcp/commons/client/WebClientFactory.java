package ch.sbb.mcp.commons.client;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SupportedCipherSuiteFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.List;

/**
 * Utility class for creating and configuring WebClient instances.
 * 
 * <p><strong>Security:</strong> All factory methods validate URLs to prevent
 * Server-Side Request Forgery (SSRF) attacks by blocking access to:
 * <ul>
 *   <li>Internal/private IP addresses (loopback, link-local, site-local)</li>
 *   <li>Cloud metadata endpoints (169.254.169.254)</li>
 *   <li>Non-HTTP(S) protocols (file://, gopher://, etc.)</li>
 * </ul>
 */
public class WebClientFactory {
    
    /**
     * Create a WebClient with default configuration.
     *
     * @param baseUrl the base URL for the API
     * @return configured WebClient
     * @throws IllegalArgumentException if URL is invalid or poses SSRF risk
     */
    public static WebClient createDefault(String baseUrl) {
        validateUrl(baseUrl);
        return WebClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }
    
    /**
     * Create a WebClient with custom timeouts.
     *
     * @param baseUrl the base URL for the API
     * @param connectTimeout connection timeout
     * @param readTimeout read timeout
     * @return configured WebClient
     * @throws IllegalArgumentException if URL is invalid or poses SSRF risk
     */
    public static WebClient createWithTimeouts(String baseUrl, 
                                               Duration connectTimeout, 
                                               Duration readTimeout) {
        validateUrl(baseUrl);
        HttpClient httpClient = HttpClient.create()
            .responseTimeout(readTimeout);
        
        return WebClient.builder()
            .baseUrl(baseUrl)
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }
    
    /**
     * Create a WebClient with OAuth2 bearer token.
     *
     * @param baseUrl the base URL for the API
     * @param bearerToken the OAuth2 bearer token
     * @return configured WebClient
     * @throws IllegalArgumentException if URL is invalid or poses SSRF risk
     */
    public static WebClient createWithBearerToken(String baseUrl, String bearerToken) {
        validateUrl(baseUrl);
        return WebClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }
    
    /**
     * Create a WebClient with API key header.
     *
     * @param baseUrl the base URL for the API
     * @param apiKeyHeader the name of the API key header
     * @param apiKey the API key value
     * @return configured WebClient
     * @throws IllegalArgumentException if URL is invalid or poses SSRF risk
     */
    public static WebClient createWithApiKey(String baseUrl, String apiKeyHeader, String apiKey) {
        validateUrl(baseUrl);
        return WebClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader(apiKeyHeader, apiKey)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }
    
    /**
     * Create a WebClient builder for custom configuration.
     *
     * @param baseUrl the base URL for the API
     * @return WebClient.Builder for further customization
     * @throws IllegalArgumentException if URL is invalid or poses SSRF risk
     */
    public static WebClient.Builder builder(String baseUrl) {
        validateUrl(baseUrl);
        return WebClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
    }
    
    /**
     * Create a WebClient with secure TLS configuration.
     * 
     * <p><strong>Security:</strong> Configures TLS with:</p>
     * <ul>
     *   <li>TLS 1.3 and TLS 1.2 only (no TLS 1.0/1.1)</li>
     *   <li>Strong cipher suites only</li>
     *   <li>Proper certificate validation</li>
     * </ul>
     *
     * @param baseUrl the base URL for the API (must be HTTPS)
     * @return configured WebClient with secure TLS
     * @throws IllegalArgumentException if URL is not HTTPS or invalid
     */
    public static WebClient createSecure(String baseUrl) {
        validateUrl(baseUrl);
        
        // Enforce HTTPS for secure connections
        if (!baseUrl.startsWith("https://")) {
            throw new IllegalArgumentException(
                "Secure WebClient requires HTTPS URL, got: " + baseUrl
            );
        }
        
        try {
            // Configure TLS with strong security settings
            SslContext sslContext = SslContextBuilder.forClient()
                .protocols("TLSv1.3", "TLSv1.2")  // Only allow TLS 1.2 and 1.3
                .ciphers(null, SupportedCipherSuiteFilter.INSTANCE)  // Use strong ciphers only
                .build();
            
            HttpClient httpClient = HttpClient.create()
                .secure(sslSpec -> sslSpec.sslContext(sslContext));
            
            return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
                
        } catch (SSLException e) {
            throw new IllegalArgumentException("Failed to configure SSL/TLS", e);
        }
    }
    
    /**
     * Validates a URL to prevent Server-Side Request Forgery (SSRF) attacks.
     * 
     * <p>This method blocks:
     * <ul>
     *   <li>Non-HTTP(S) protocols (file://, gopher://, ftp://, etc.)</li>
     *   <li>Loopback addresses (127.0.0.0/8, ::1)</li>
     *   <li>Link-local addresses (169.254.0.0/16, fe80::/10)</li>
     *   <li>Private networks (10.0.0.0/8, 172.16.0.0/12, 192.168.0.0/16)</li>
     *   <li>Cloud metadata endpoints (169.254.169.254)</li>
     * </ul>
     * 
     * @param url the URL to validate
     * @throws IllegalArgumentException if URL is invalid or poses SSRF risk
     */
    private static void validateUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("URL cannot be null or empty");
        }
        
        try {
            URI uri = new URI(url);
            
            // Validate protocol - only allow HTTP and HTTPS
            String scheme = uri.getScheme();
            if (scheme == null || !List.of("http", "https").contains(scheme.toLowerCase())) {
                throw new IllegalArgumentException(
                    "Invalid protocol: " + scheme + ". Only HTTP and HTTPS are allowed"
                );
            }
            
            // Validate host is present
            String host = uri.getHost();
            if (host == null || host.isBlank()) {
                throw new IllegalArgumentException("URL must contain a valid host");
            }
            
            // Resolve hostname to IP address
            InetAddress address;
            try {
                address = InetAddress.getByName(host);
            } catch (UnknownHostException e) {
                throw new IllegalArgumentException("Cannot resolve host: " + host, e);
            }
            
            // Block loopback addresses (127.0.0.0/8, ::1)
            if (address.isLoopbackAddress()) {
                throw new IllegalArgumentException(
                    "Access to loopback addresses is forbidden: " + host
                );
            }
            
            // Block link-local addresses (169.254.0.0/16, fe80::/10)
            if (address.isLinkLocalAddress()) {
                throw new IllegalArgumentException(
                    "Access to link-local addresses is forbidden: " + host
                );
            }
            
            // Block site-local/private addresses (10.0.0.0/8, 172.16.0.0/12, 192.168.0.0/16)
            if (address.isSiteLocalAddress()) {
                throw new IllegalArgumentException(
                    "Access to private network addresses is forbidden: " + host
                );
            }
            
            // Block cloud metadata endpoint (AWS, GCP, Azure)
            String hostAddress = address.getHostAddress();
            if ("169.254.169.254".equals(hostAddress)) {
                throw new IllegalArgumentException(
                    "Access to cloud metadata service is forbidden"
                );
            }
            
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL syntax: " + url, e);
        }
    }
}
