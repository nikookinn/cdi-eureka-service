package com.dafreurekadetails;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:application-test.properties")
class DafEurekaDetailsApplicationTests {

	@Test
	void contextLoads() {
	}

}
