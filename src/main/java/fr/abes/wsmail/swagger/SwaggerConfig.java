package fr.abes.wsmail.swagger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("fr.abes.wsmail.controller"))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(metadata());
    }

    private ApiInfo metadata() {
        return new ApiInfoBuilder()
                .title("WebService Envoi de mail Abes")
                .description("Ce WS vous permet d'envoyer des mail depuis n'importe quelle application, via un simple appel REST. <br />" +
                        "Des exemples d'appel Java et JS à ce WS sont disponibles dans le readme du dépot : <a href='https://git.abes.fr/api-communes/WsAbesMail'>https://git.abes.fr/api-communes/WsAbesMail.</a> <br />" +
                        "Il est nécessaire d'ajouter les comptes mails de vos applications au WS avant de l'utiliser. C'est rapide, contacter moi ou quelqu'un au SCOD afin que ce soit fait.")
                .version("1.0.0")
                .contact(new Contact("TCN", "https://git.abes.fr/api-communes/WsAbesMail", "tcn@abes.fr"))
                .build();
    }
}
