package com.remindercat.wechat;

import com.remindercat.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Slf4j
@Component
public class WeChatClient {

    private static final String BASE_URL = "https://qyapi.weixin.qq.com";

    private final WeChatProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public WeChatClient(WeChatProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder().baseUrl(BASE_URL).build();
    }

    public void sendReminder(String userId, String content) {
        validateConfiguration();

        try {
            String accessToken = getAccessToken();
            Map<String, Object> requestBody = Map.of(
                    "touser", userId,
                    "msgtype", "text",
                    "agentid", properties.getAgentId(),
                    "text", Map.of("content", content),
                    "safe", 0
            );

            String responseBody = restClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/cgi-bin/message/send")
                            .queryParam("access_token", accessToken)
                            .build())
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            verifySuccess(responseBody, "企业微信消息发送失败");
            log.info("WeChat reminder sent, userId={}", userId);
        } catch (BusinessException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new BusinessException(502, "企业微信HTTP调用失败");
        } catch (RuntimeException exception) {
            throw new BusinessException(502, "企业微信响应解析失败");
        }
    }

    private String getAccessToken() {
        String responseBody = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/cgi-bin/gettoken")
                        .queryParam("corpid", properties.getCorpId())
                        .queryParam("corpsecret", properties.getSecret())
                        .build())
                .retrieve()
                .body(String.class);

        JsonNode root = verifySuccess(responseBody, "企业微信凭证获取失败");
        String accessToken = root.path("access_token").asText();
        if (!StringUtils.hasText(accessToken)) {
            throw new BusinessException(502, "企业微信响应缺少access_token");
        }
        return accessToken;
    }

    private JsonNode verifySuccess(String responseBody, String errorMessage) {
        if (!StringUtils.hasText(responseBody)) {
            throw new BusinessException(502, errorMessage + "：空响应");
        }

        JsonNode root = objectMapper.readTree(responseBody);
        if (root.path("errcode").asInt(-1) != 0) {
            String detail = root.path("errmsg").asText();
            throw new BusinessException(502, errorMessage + "：" + detail);
        }
        return root;
    }

    private void validateConfiguration() {
        if (!StringUtils.hasText(properties.getCorpId())) {
            throw new BusinessException(500, "企业微信corp-id未配置");
        }
        if (!StringUtils.hasText(properties.getSecret())) {
            throw new BusinessException(500, "企业微信secret未配置");
        }
        if (properties.getAgentId() == null || properties.getAgentId() <= 0) {
            throw new BusinessException(500, "企业微信agent-id未配置");
        }
    }
}
