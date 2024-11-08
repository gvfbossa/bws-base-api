package br.com.bossawebsolutions.base_api.infrastructure.web.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração Spring para fornecer a instância personalizada de {@link ObjectMapper} via injeção de dependência.
 */
@Configuration
public class CustomObjectMapperConfig {

    /**
     * @return a instância de {@link ObjectMapper} configurada.
     */
    @Bean
    ObjectMapper objectMapper() {
        return CustomObjectMapper.getInstance();
    }
}
