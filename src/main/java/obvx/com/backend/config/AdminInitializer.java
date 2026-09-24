package obvx.com.backend.config;

import obvx.com.backend.service.AuthService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminInitializer {

    @Bean
    CommandLineRunner createAdmin(AuthService authService) {
        return args -> {

            authService.makeAdmin("christ@obvx.com");

        };
    }
}