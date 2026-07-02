package com.cityscout.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI cityScoutOpenApi() {
        return new OpenAPI().info(new Info()
                .title("CityScout API")
                .version("0.1.0")
                .description("REST API путеводителя по городу: точки интереса, оценки и отзывы"));
    }
}
