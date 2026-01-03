package com.discovery;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class EurekaServerApplicationTests {

	@AfterEach
	void tearDown() {
		EurekaServerApplication.stop();
	}

	@Test
	void mainStartsEurekaServerOnRandomPort() {
		assertDoesNotThrow(() -> EurekaServerApplication.main(new String[]{
				"--server.port=0",
				"--eureka.client.register-with-eureka=false",
				"--eureka.client.fetch-registry=false"
		}));

		ConfigurableApplicationContext context = EurekaServerApplication.getContext();
		assertThat(context).isNotNull();
		assertThat(context.isRunning()).isTrue();

		EurekaServerApplication.stop();
		assertThat(EurekaServerApplication.getContext()).isNull();
	}

	@Test
	void stopIsSafeWhenNoContextIsInitialized() {
		assertDoesNotThrow(EurekaServerApplication::stop);
	}
}
