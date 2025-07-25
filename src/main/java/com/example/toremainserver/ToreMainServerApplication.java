package com.example.toremainserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import com.example.toremainserver.repository.UserRepository;
import com.example.toremainserver.entity.User;
import java.util.Optional;

@SpringBootApplication
public class ToreMainServerApplication {

    public static void main(String[] args) {
        System.out.println("Hello");
        SpringApplication.run(ToreMainServerApplication.class, args);
    }

    // RestTemplate 빈 등록
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public CommandLineRunner testDbConnection(UserRepository userRepository) {
        return args -> {
            Optional<User> adminUser = userRepository.findByUsername("admin");
            if (adminUser.isPresent()) {
                User user = adminUser.get();
                System.out.println("Admin User ID: " + user.getId());
                System.out.println("Admin Username: " + user.getUsername());
                System.out.println("Admin Password: " + user.getPassword());
            } else {
                System.out.println("Admin user not found.");
            }
        };
    }
}
