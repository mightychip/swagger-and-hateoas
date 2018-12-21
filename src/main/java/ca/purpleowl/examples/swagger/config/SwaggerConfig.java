package ca.purpleowl.examples.swagger.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


@Configuration
@EnableSwagger2
public class SwaggerConfig {

    /**
     * A we bit o' swagger config.  We could, and in production SHOULD, be more specific than this, but I'm doing this
     * quickly to provide an example... I may come back and lock this down with security and stuff later for more
     * awesome examples.
     */
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                //Yeah yeah... fuck you, too, Swagger.  Do what I want.
                .directModelSubstitute(ResponseEntity.class, Void.class)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build();
    }
}
