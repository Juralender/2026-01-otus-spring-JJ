package com.kshaitry.userlist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class UserlistApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(UserlistApplication.class, args);

		if (context.getEnvironment().getProperty("app.exit-after-startup", Boolean.class, false)) {
			System.exit(SpringApplication.exit(context));
		}
	}

}
