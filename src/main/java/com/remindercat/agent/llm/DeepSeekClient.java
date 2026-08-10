package com.remindercat.agent.llm;

import com.remindercat.agent.llm.exception.LLMCallException;
import com.remindercat.agent.llm.exception.LLMConfigurationException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Component
public class DeepSeekClient implements LLMClient {

    private final LLMProperties properties;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public DeepSeekClient(
            LLMProperties properties,
            ObjectMapper objectMapper
    ) {
        this.properties = properties;
        this.restClient = RestClient.builder().baseUrl(properties.getBaseUrl()).build();
        this.objectMapper = objectMapper;
    }

    @Override
    public String chat(String prompt) {
        validateConfiguration();

        Map<String, Object> requestBody = Map.of(
                "model", properties.getModel(),
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "stream", false
        );

        try {
            String responseBody = restClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + properties.getApiKey())
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            return extractModelText(responseBody);
        } catch (LLMCallException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new LLMCallException("DeepSeek HTTP调用失败", exception);
        } catch (RuntimeException exception) {
            throw new LLMCallException("DeepSeek响应解析失败", exception);
        }
    }

    private void validateConfiguration() {
        if (!StringUtils.hasText(properties.getApiKey())) {
            throw new LLMConfigurationException("DeepSeek API key未配置");
        }
        if (!StringUtils.hasText(properties.getBaseUrl())) {
            throw new LLMConfigurationException("DeepSeek base URL未配置");
        }
        if (!StringUtils.hasText(properties.getModel())) {
            throw new LLMConfigurationException("DeepSeek model未配置");
        }
    }

    private String extractModelText(String responseBody) {
        if (!StringUtils.hasText(responseBody)) {
            throw new LLMCallException("DeepSeek返回空响应");
        }

        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
        String content = contentNode.asText();
        if (!StringUtils.hasText(content)) {
            throw new LLMCallException("DeepSeek响应缺少模型文本");
        }
        return content;
    }
}
