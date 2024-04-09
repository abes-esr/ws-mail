package fr.abes.wsmail.swagger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI usersMicroserviceOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("WebService Envoi de mail Abes")
                        .description("Ce WS vous permet d'envoyer des mail depuis n'importe quelle application, via un simple appel REST. <br />" +
                                "Des exemples d'appel Java et JS à ce WS sont disponibles dans le readme du dépot.")
                        .version("1.0"));
    }
}