package com.remindercat.wechat;

import com.remindercat.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.cp.api.WxCpService;
import me.chanjar.weixin.cp.api.impl.WxCpServiceImpl;
import me.chanjar.weixin.cp.bean.message.WxCpMessage;
import me.chanjar.weixin.cp.bean.message.WxCpMessageSendResult;
import me.chanjar.weixin.cp.config.impl.WxCpDefaultConfigImpl;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
public class WeChatClient {

    private final WeChatProperties properties;

    public WeChatClient(WeChatProperties properties) {
        this.properties = properties;
    }

    public void sendReminder(String userId, String content) {
        validateConfiguration();

        try {
            WxCpDefaultConfigImpl config = new WxCpDefaultConfigImpl();
            config.setCorpId(properties.getCorpId());
            config.setCorpSecret(properties.getSecret());
            config.setAgentId(properties.getAgentId());

            WxCpService wxCpService = new WxCpServiceImpl();
            wxCpService.setWxCpConfigStorage(config);

            WxCpMessage message = WxCpMessage.TEXT()
                    .agentId(properties.getAgentId())
                    .toUser(userId)
                    .content(content)
                    .build();
            WxCpMessageSendResult result = wxCpService.getMessageService().send(message);

            if (result.getInvalidUserList() != null
                    && result.getInvalidUserList().contains(userId)) {
                throw new BusinessException(502, "企业微信接收用户无效");
            }
            log.info("WeChat reminder sent, userId={}", userId);
        } catch (BusinessException exception) {
            throw exception;
        } catch (WxErrorException exception) {
            log.error("WeChat reminder API call failed, userId={}, errorCode={}",
                    userId,
                    exception.getError() == null ? null : exception.getError().getErrorCode());
            throw new BusinessException(502, "企业微信消息发送失败");
        } catch (RuntimeException exception) {
            throw new BusinessException(502, "企业微信消息发送异常");
        }
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
