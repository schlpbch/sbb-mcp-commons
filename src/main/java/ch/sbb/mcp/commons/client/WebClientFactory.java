package ch.sbb.mcp.commons.client;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

/**
 * Utility class for creating and configuring WebClient instances.
 */
public class WebClientFactory {
    
    /**
     * Create a WebClient with default configuration.
     *
     * @param baseUrl the base URL for the API
     * @return configured WebClient
     */
    public static WebClient createDefault(String baseUrl) {
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
     */
    public static WebClient createWithTimeouts(String baseUrl, 
                                               Duration connectTimeout, 
                                               Duration readTimeout) {
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
     */
    public static WebClient createWithBearerToken(String baseUrl, String bearerToken) {
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
     */
    public static WebClient createWithApiKey(String baseUrl, String apiKeyHeader, String apiKey) {
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
     */
    public static WebClient.Builder builder(String baseUrl) {
        return WebClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
    }
}
