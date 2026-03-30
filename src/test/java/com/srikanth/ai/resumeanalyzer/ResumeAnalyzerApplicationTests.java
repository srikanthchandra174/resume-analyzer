package com.srikanth.ai.resumeanalyzer;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Base test class for integration tests
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.yml")
public class ResumeAnalyzerApplicationTests {

}

