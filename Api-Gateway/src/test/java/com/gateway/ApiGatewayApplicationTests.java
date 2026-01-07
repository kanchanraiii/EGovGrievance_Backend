package com.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;
class ApiGatewayApplicationTests {

	@Test
	void contextLoads() {
		assertThat(ApiGatewayApplication.class.getPackageName()).isEqualTo("com.gateway");
	}

	@Test
	void defaultConstructorExists() {
		assertThat(new ApiGatewayApplication()).isNotNull();
	}

	@Test
	void mainDelegatesToSpringApplicationRun() {
		try (MockedStatic<SpringApplication> mocked = Mockito.mockStatic(SpringApplication.class)) {
			mocked.when(() -> SpringApplication.run(
					Mockito.eq(ApiGatewayApplication.class),
					Mockito.<String[]>any()))
					.thenReturn(null);

			ApiGatewayApplication.main(new String[] {"--spring.main.web-application-type=none"});

			mocked.verify(() -> SpringApplication.run(
					Mockito.eq(ApiGatewayApplication.class),
					Mockito.<String[]>any()));
		}
	}

}
