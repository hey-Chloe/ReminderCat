package com.remindercat.agent.llm;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "deepseek")
public class LLMProperties {

    private String apiKey;

    private String baseUrl;

    private String model;
}
