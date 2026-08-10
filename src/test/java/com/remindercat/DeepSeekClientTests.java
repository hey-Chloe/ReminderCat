package com.remindercat;

import com.remindercat.agent.llm.DeepSeekClient;
import com.remindercat.agent.llm.LLMClient;
import com.remindercat.agent.llm.LLMProperties;
import com.remindercat.agent.llm.MockLLMClient;
import com.remindercat.agent.llm.exception.LLMConfigurationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
        "deepseek.api-key=test-key",
        "deepseek.base-url=https://example.test",
        "deepseek.model=test-model"
})
class DeepSeekClientTests {

    @Autowired
    private LLMProperties properties;

    @Test
    void shouldBindDeepSeekConfiguration() {
        assertThat(properties.getApiKey()).isEqualTo("test-key");
        assertThat(properties.getBaseUrl()).isEqualTo("https://example.test");
        assertThat(properties.getModel()).isEqualTo("test-model");
    }

    @Test
    void shouldRejectMissingApiKeyWithoutCallingNetwork() {
        LLMProperties missingKeyProperties = new LLMProperties();
        missingKeyProperties.setApiKey("");
        missingKeyProperties.setBaseUrl("https://example.test");
        missingKeyProperties.setModel("test-model");

        LLMClient client = new DeepSeekClient(
                missingKeyProperties,
                new ObjectMapper()
        );

        assertThatThrownBy(() -> client.chat("hello"))
                .isInstanceOf(LLMConfigurationException.class)
                .hasMessage("DeepSeek API key未配置");
    }

    @Test
    void shouldAllowMockClientReplacement() {
        LLMClient client = new MockLLMClient("mock-response");

        assertThat(client.chat("hello")).isEqualTo("mock-response");
    }
}
