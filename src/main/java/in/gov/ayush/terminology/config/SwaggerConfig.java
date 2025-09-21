package in.gov.ayush.terminology.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("NAMASTE-ICD11 FHIR Terminology Service API")
                        .description("FHIR R4 compliant terminology service for integrating NAMASTE codes with WHO ICD-11 Traditional Medicine Module 2 and Biomedicine codes. Supports dual coding, concept mapping, and ABHA-secured access.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Ministry of AYUSH")
                                .url("https://ayush.gov.in")
                                .email("support@ayush.gov.in"))
                        .license(new License()
                                .name("Government of India License")
                                .url("https://www.gov.in/rules-and-regulations")))
                .servers(Arrays.asList(
                        new Server().url("http://localhost:8080/fhir-terminology").description("Development Server"),
                        new Server().url("https://api.ayush.gov.in/fhir-terminology").description("Production Server")))
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("ABHA JWT Token Authentication")))
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
    }
}
