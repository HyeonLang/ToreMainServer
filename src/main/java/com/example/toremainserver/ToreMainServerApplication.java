package com.example.toremainserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

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
}
