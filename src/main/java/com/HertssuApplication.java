package com;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.time.Clock;

@SpringBootApplication
public class HertssuApplication {

	public static void main(String[] args) {
		SpringApplication.run(HertssuApplication.class, args);
	}

	@Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

	@Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
