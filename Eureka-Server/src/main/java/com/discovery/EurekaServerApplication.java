package com.discovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

	private static ConfigurableApplicationContext context;

	public static void main(String[] args) {
		start(args);
	}

	static synchronized ConfigurableApplicationContext start(String... args) {
		stop();
		context = SpringApplication.run(EurekaServerApplication.class, args);
		return context;
	}

	static synchronized void stop() {
		if (context != null) {
			context.close();
			context = null;
		}
	}

	static synchronized ConfigurableApplicationContext getContext() {
		return context;
	}

}
