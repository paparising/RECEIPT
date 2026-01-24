package com.example.receipt;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
	"sendgrid.api.key=test_key_12345",
	"sendgrid.from.email=test@example.com"
})
class ReceiptApplicationTests {

	@Test
	void contextLoads() {
	}

}
