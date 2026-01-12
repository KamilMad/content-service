package pl.kamil.content_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import pl.kamil.content_service.util.TokenRelayInterceptor;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient restClient(RestClient.Builder builder) {
        return builder
                .requestInterceptor(new TokenRelayInterceptor())
                .build();
    }
}
