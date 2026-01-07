package com.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;
class AuthServiceApplicationTests {

	@Test
	void contextLoads() {
		assertThat(AuthServiceApplication.class.getPackageName()).isEqualTo("com.auth");
	}

	@Test
	void mainDelegatesToSpringApplicationRun() {
		try (MockedStatic<SpringApplication> mocked = Mockito.mockStatic(SpringApplication.class)) {
			mocked.when(() -> SpringApplication.run(
					Mockito.eq(AuthServiceApplication.class),
					Mockito.<String[]>any()))
					.thenReturn(null);

			AuthServiceApplication.main(new String[]{"--spring.main.web-application-type=none"});

			mocked.verify(() -> SpringApplication.run(
					Mockito.eq(AuthServiceApplication.class),
					Mockito.<String[]>any()));
		}
	}

	@Test
	void constructorExists() {
		assertThat(new AuthServiceApplication()).isNotNull();
	}

}
