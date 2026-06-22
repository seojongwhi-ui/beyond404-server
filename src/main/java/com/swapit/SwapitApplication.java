package com.swapit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SwapitApplication {

	public static void main(String[] args) {
		SpringApplication.run(SwapitApplication.class, args);
	}

}
