package ca.purpleowl.examples.swagger.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories("ca.purpleowl.examples.swagger.jpa.repository")
public class JpaRepositoryConfig {
}
