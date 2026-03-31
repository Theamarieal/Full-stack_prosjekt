package ntnu.no.fs_v26.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

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