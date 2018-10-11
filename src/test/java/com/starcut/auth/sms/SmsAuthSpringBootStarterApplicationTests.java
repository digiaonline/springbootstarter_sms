package com.starcut.auth.sms;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestConfiguration(value = "com.starcut.auth.sms.config.SmsAuthAutoConfiguration")
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
public class SmsAuthSpringBootStarterApplicationTests {

	@Test
	public void contextLoads() {
	}

}
