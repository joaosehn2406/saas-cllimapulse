package com.climapulse.jceco;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "climapulse.inpe.catch-up-enabled=false")
class JcecoApplicationTests {

	@Test
	void contextLoads() {
	}

}