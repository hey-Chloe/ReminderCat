package com.remindercat.wechat;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "wechat")
public class WeChatProperties {

    private String corpId;

    private String secret;

    private Integer agentId;

    private String token;

    private String encodingAesKey;
}
