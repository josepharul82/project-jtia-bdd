package com.bff.config;

import com.bff.properties.BffProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Configures the RestClient bean used by ProxyController to forward requests
 * to the backend.
 *
 * RestClient is the modern synchronous HTTP client introduced in Spring 6.
 * It replaces RestTemplate with a cleaner fluent API while remaining fully
 * blocking (no reactive types).
 */
@Configuration
public class ProxyConfig {

    @Bean
    public RestClient.Builder restClientBuilder(BffProperties props) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(props.proxy().connectTimeoutMs());
        factory.setReadTimeout(props.proxy().readTimeoutMs());

        return RestClient.builder()
                .requestFactory(factory);
    }
}
