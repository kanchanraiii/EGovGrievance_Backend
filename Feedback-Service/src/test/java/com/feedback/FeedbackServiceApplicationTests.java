package com.feedback;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class FeedbackServiceApplicationTests {

	@AfterEach
	void tearDown() {
		FeedbackServiceApplication.stop();
	}

	@Test
	void mainStartsAndStopsApplicationContext() {
		assertDoesNotThrow(() -> FeedbackServiceApplication.main(new String[]{
				"--server.port=0",
				"--spring.cloud.discovery.enabled=false",
				"--eureka.client.enabled=false",
				"--spring.main.web-application-type=reactive",
				"--spring.main.lazy-initialization=true"
		}));

		ConfigurableApplicationContext context = FeedbackServiceApplication.getContext();
		assertThat(context).isNotNull();
		assertThat(context.isRunning()).isTrue();

		FeedbackServiceApplication.stop();
		assertThat(FeedbackServiceApplication.getContext()).isNull();
	}

	@Test
	void stopIsSafeWhenContextNotStarted() {
		assertDoesNotThrow(FeedbackServiceApplication::stop);
		assertThat(FeedbackServiceApplication.getContext()).isNull();
	}
}
