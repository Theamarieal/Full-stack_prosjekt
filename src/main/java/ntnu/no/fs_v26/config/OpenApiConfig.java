package ntnu.no.fs_v26.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for OpenAPI (Swagger) documentation.
 *
 * <p>Defines the metadata displayed in the Swagger UI, including the API title,
 * description, version, and contact information.
 *
 * <p>The Swagger UI is available at {@code /swagger-ui/index.html} when the application
 * is running.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Creates and configures the {@link OpenAPI} bean with project metadata.
     *
     * @return a configured {@link OpenAPI} instance
     */
    @Bean
    public OpenAPI myOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Restaurant Management API")
                .description("API for managing restaurant staff, orders, and inventory.")
                .version("1.0")
                .contact(new Contact()
                    .name("Prosjektgruppe FS")
                    .email("kontakt@ntnu.no")));
    }
}