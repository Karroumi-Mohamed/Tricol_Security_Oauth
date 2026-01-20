package com.tricol;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TricolApplication {

	public static void main(String[] args) {
		SpringApplication.run(TricolApplication.class, args);
	}

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {
            System.out.println("Application started successfully!");
            for (String name : ctx.getBeanDefinitionNames()) {
                if (name.toLowerCase().contains("supplier")) {
                    System.out.println("Bean found: " + name);
                }
            }
        };
    }
}
