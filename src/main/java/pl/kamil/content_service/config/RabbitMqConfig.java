package pl.kamil.content_service.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Bean
    public Queue uploadQueue() {
        return new Queue("uploadQueue", true);
    }
}
