package assembly.general.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables JPA auditing so @CreatedDate and @LastModifiedDate
 * are automatically populated on entity save/update.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
