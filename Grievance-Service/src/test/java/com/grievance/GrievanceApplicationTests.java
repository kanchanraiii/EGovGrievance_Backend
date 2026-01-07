package com.grievance;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GrievanceApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void mainDelegatesToSpringApplicationRun() {
		assertThat(GrievanceApplication.class).isNotNull();
	}

	@Test
	void constructorExists() {
		assertThat(new GrievanceApplication()).isNotNull();
	}

}
