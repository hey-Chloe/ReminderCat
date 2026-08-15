package com.remindercat;

import com.remindercat.agent.AgentState;
import com.remindercat.wechat.WeChatMessageProcessor;
import me.chanjar.weixin.common.util.crypto.SHA1;
import me.chanjar.weixin.cp.config.impl.WxCpDefaultConfigImpl;
import me.chanjar.weixin.cp.util.crypto.WxCpCryptUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "wechat.corp-id=ww-test-corp",
        "wechat.secret=test-secret",
        "wechat.agent-id=1000002",
        "wechat.token=ReminderCatTestToken",
        "wechat.encoding-aes-key=abcdefghijklmnopqrstuvwxyz0123456789ABCDEFG"
})
@AutoConfigureMockMvc
class WeChatControllerTests {

    private static final String CORP_ID = "ww-test-corp";

    private static final String TOKEN = "ReminderCatTestToken";

    private static final String AES_KEY = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFG";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WeChatMessageProcessor weChatMessageProcessor;

    private WxCpCryptUtil cryptUtil;

    @BeforeEach
    void setUp() {
        WxCpDefaultConfigImpl config = new WxCpDefaultConfigImpl();
        config.setCorpId(CORP_ID);
        config.setToken(TOKEN);
        config.setAesKey(AES_KEY);
        cryptUtil = new WxCpCryptUtil(config);
    }

    @Test
    void verifyShouldReturnDecryptedEchostrWhenSignatureIsValid() throws Exception {
        String plaintext = "hello-remindercat";
        String timestamp = "1700000000";
        String nonce = "test-nonce";
        String echostr = cryptUtil.encrypt(randomString(), plaintext);
        String msgSignature = SHA1.gen(TOKEN, timestamp, nonce, echostr);

        mockMvc.perform(get("/wechat/message")
                        .param("msg_signature", msgSignature)
                        .param("timestamp", timestamp)
                        .param("nonce", nonce)
                        .param("echostr", echostr))
                .andExpect(status().isOk())
                .andExpect(content().string(plaintext));
    }

    @Test
    void verifyShouldRejectInvalidSignature() throws Exception {
        String timestamp = "1700000000";
        String nonce = "test-nonce";
        String echostr = cryptUtil.encrypt(randomString(), "hello-remindercat");

        mockMvc.perform(get("/wechat/message")
                        .param("msg_signature", "invalid-signature")
                        .param("timestamp", timestamp)
                        .param("nonce", nonce)
                        .param("echostr", echostr))
                .andExpect(status().isForbidden());
    }

    @Test
    void receiveShouldProcessTextMessageOnce() throws Exception {
        EncryptedMessage message = encryptMessage("text", "wx-user-001", "明天下午3点提醒我开会");

        message.perform(mockMvc)
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(weChatMessageProcessor, times(1)).processAsync(argThat(state ->
                "wx-user-001".equals(state.getUserId())
                        && "明天下午3点提醒我开会".equals(state.getInput())));
    }

    @Test
    void receiveShouldIgnoreDuplicateMsgId() throws Exception {
        EncryptedMessage message = encryptMessage("text", "wx-user-001", "明天下午3点提醒我开会");

        message.perform(mockMvc).andExpect(status().isOk());
        message.perform(mockMvc).andExpect(status().isOk());

        verify(weChatMessageProcessor, times(1)).processAsync(any(AgentState.class));
    }

    @Test
    void receiveShouldNotProcessNonTextMessage() throws Exception {
        EncryptedMessage message = encryptMessage("image", "wx-user-001", "");

        message.perform(mockMvc)
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(weChatMessageProcessor, never()).processAsync(any(AgentState.class));
    }

    @Test
    void receiveShouldRejectInvalidSignature() throws Exception {
        String timestamp = "1700000000";
        String nonce = "test-nonce";
        String encrypted = cryptUtil.encrypt(
                randomString(),
                textMessageXml("wx-user-001", "text", "明天下午3点提醒我开会", "msg-1")
        );

        mockMvc.perform(post("/wechat/message")
                        .param("msg_signature", "invalid-signature")
                        .param("timestamp", timestamp)
                        .param("nonce", nonce)
                        .contentType(MediaType.TEXT_XML)
                        .content(encryptedXml(encrypted)))
                .andExpect(status().isForbidden());

        verify(weChatMessageProcessor, never()).processAsync(any(AgentState.class));
    }

    private EncryptedMessage encryptMessage(String msgType, String fromUser, String content) {
        String msgId = String.valueOf(System.nanoTime());
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String nonce = "nonce-" + UUID.randomUUID();
        String encrypted = cryptUtil.encrypt(randomString(), textMessageXml(fromUser, msgType, content, msgId));
        String msgSignature = SHA1.gen(TOKEN, timestamp, nonce, encrypted);
        return new EncryptedMessage(encryptedXml(encrypted), msgSignature, timestamp, nonce);
    }

    private String textMessageXml(String fromUser, String msgType, String content, String msgId) {
        return """
                <xml>
                  <ToUserName><![CDATA[%s]]></ToUserName>
                  <FromUserName><![CDATA[%s]]></FromUserName>
                  <CreateTime>1700000000</CreateTime>
                  <MsgType><![CDATA[%s]]></MsgType>
                  <Content><![CDATA[%s]]></Content>
                  <MsgId>%s</MsgId>
                  <AgentID>1000002</AgentID>
                </xml>
                """.formatted(CORP_ID, fromUser, msgType, content, msgId);
    }

    private String encryptedXml(String encrypted) {
        return """
                <xml>
                  <ToUserName><![CDATA[%s]]></ToUserName>
                  <AgentID><![CDATA[1000002]]></AgentID>
                  <Encrypt><![CDATA[%s]]></Encrypt>
                </xml>
                """.formatted(CORP_ID, encrypted);
    }

    private static String randomString() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    private record EncryptedMessage(String body, String msgSignature, String timestamp, String nonce) {
        ResultActions perform(MockMvc mockMvc) throws Exception {
            return mockMvc.perform(post("/wechat/message")
                    .param("msg_signature", msgSignature)
                    .param("timestamp", timestamp)
                    .param("nonce", nonce)
                    .contentType(MediaType.TEXT_XML)
                    .content(body));
        }
    }
}
