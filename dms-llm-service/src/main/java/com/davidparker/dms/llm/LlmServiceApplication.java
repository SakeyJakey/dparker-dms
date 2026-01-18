package com.davidparker.dms.llm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = "com.davidparker.dms.llm")
@EnableAsync
public class LlmServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LlmServiceApplication.class, args);
    }
}
