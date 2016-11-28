package com.gjermundbjaanes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

@SpringBootApplication
public class GoodreadsBackupWebApplication extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(GoodreadsBackupWebApplication.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(GoodreadsBackupWebApplication.class, args);
	}
}
