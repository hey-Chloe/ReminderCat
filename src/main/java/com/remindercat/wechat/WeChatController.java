package com.remindercat.wechat;

import com.remindercat.agent.AgentState;
import com.remindercat.repository.WeChatMessageRepository;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.cp.api.WxCpService;
import me.chanjar.weixin.cp.api.impl.WxCpServiceImpl;
import me.chanjar.weixin.cp.bean.message.WxCpXmlMessage;
import me.chanjar.weixin.cp.config.impl.WxCpDefaultConfigImpl;
import me.chanjar.weixin.cp.util.crypto.WxCpCryptUtil;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/wechat")
public class WeChatController {


    private final WeChatProperties properties;
    private final WeChatMessageRepository weChatMessageRepository;
    private final WeChatMessageProcessor weChatMessageProcessor;

    public WeChatController(
            WeChatProperties properties,
            WeChatMessageRepository weChatMessageRepository,
            WeChatMessageProcessor weChatMessageProcessor
    ) {
        this.properties = properties;
        this.weChatMessageRepository = weChatMessageRepository;
        this.weChatMessageProcessor = weChatMessageProcessor;
    }


    /**
     * 企业微信URL验证
     */
    @GetMapping(value = "/message", produces = "text/plain;charset=UTF-8")
    public String verify(
            @RequestParam("msg_signature") String msgSignature,
            @RequestParam("timestamp") String timestamp,
            @RequestParam("nonce") String nonce,
            @RequestParam("echostr") String echostr
    ) {


        log.info("收到企业微信URL验证请求");


        WxCpService wxCpService = new WxCpServiceImpl();
        wxCpService.setWxCpConfigStorage(buildConfig());

        if (!wxCpService.checkSignature(msgSignature, timestamp, nonce, echostr)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "企业微信回调签名校验失败");
        }

        return new WxCpCryptUtil(buildConfig()).decrypt(echostr);
    }


    /**
     * 接收微信消息
     */
    @PostMapping("/message")
    public String receive(
            @RequestParam("msg_signature") String msgSignature,
            @RequestParam("timestamp") String timestamp,
            @RequestParam("nonce") String nonce,
            @RequestBody String body
    ) {
        WxCpXmlMessage message;
        try {
            message = WxCpXmlMessage.fromEncryptedXml(body, buildConfig(), timestamp, nonce, msgSignature);
        } catch (RuntimeException exception) {
            log.warn("企业微信消息验签或解密失败", exception);
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "企业微信消息验签或解密失败"
            );
        }

        log.info("收到企业微信消息, msgType={}, msgId={}", message.getMsgType(), message.getMsgId());

        Long rawMsgId = message.getMsgId();
        String msgId = rawMsgId == null ? null : rawMsgId.toString();
        if (StringUtils.hasText(msgId)) {
            boolean firstTime = weChatMessageRepository.markReceived(
                    msgId,
                    message.getFromUserName(),
                    message.getMsgType(),
                    LocalDateTime.now()
            );
            if (!firstTime) {
                log.info("重复的企业微信回调已忽略, msgId={}", msgId);
                return "";
            }
        }

        if (!"text".equals(message.getMsgType())) {
            log.info("暂不处理非文本企业微信消息, msgType={}", message.getMsgType());
            return "";
        }

        if (!StringUtils.hasText(message.getFromUserName())
                || !StringUtils.hasText(message.getContent())) {
            log.warn("企业微信文本消息缺少发送者或内容, msgId={}", msgId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "企业微信文本消息格式不完整");
        }

        AgentState state = AgentState.builder()
                .userId(message.getFromUserName())
                .input(message.getContent())
                .build();
        weChatMessageProcessor.processAsync(state);

        log.info("企业微信文本消息已提交异步处理, msgId={}", msgId);
        return "";
    }

    private WxCpDefaultConfigImpl buildConfig() {
        if (!StringUtils.hasText(properties.getCorpId())
                || !StringUtils.hasText(properties.getToken())
                || !StringUtils.hasText(properties.getEncodingAesKey())) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "企业微信回调配置缺失");
        }
        WxCpDefaultConfigImpl config = new WxCpDefaultConfigImpl();
        config.setCorpId(properties.getCorpId());
        config.setToken(properties.getToken());
        config.setAesKey(properties.getEncodingAesKey());
        return config;
    }

}
