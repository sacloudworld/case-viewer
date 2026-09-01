package com.example.case_viewer.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI caseViewerOpenAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title("Case Viewer API")
                        .description("REST API for Case Viewer")
                        .version("1.0.0"))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local"),

                        new Server()
                                .url("https://dev-api.example.com")
                                .description("Development"),

                        new Server()
                                .url("https://qa-api.example.com")
                                .description("QA"),

                        new Server()
                                .url("https://api.example.com")
                                .description("Production")
                ));
    }
}