package com.remindercat;

import com.remindercat.agent.AgentResult;
import com.remindercat.agent.AgentRuntime;
import com.remindercat.agent.Intent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class WeChatControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AgentRuntime agentRuntime;

    @Test
    void webhookShouldInvokeAgentRuntimeAndReturnUnifiedResult() throws Exception {
        when(agentRuntime.execute(argThat(state ->
                "wx-user-001".equals(state.getUserId())
                        && "明天下午3点提醒我开会".equals(state.getInput()))))
                .thenReturn(AgentResult.success(Intent.CREATE_TASK, "任务创建成功", null));

        mockMvc.perform(post("/wechat/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": "wx-user-001",
                                  "message": "明天下午3点提醒我开会"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.success").value(true))
                .andExpect(jsonPath("$.data.intent").value("CREATE_TASK"));

        verify(agentRuntime).execute(argThat(state ->
                "wx-user-001".equals(state.getUserId())
                        && "明天下午3点提醒我开会".equals(state.getInput())));
    }

    @Test
    void webhookShouldRejectBlankMessage() throws Exception {
        mockMvc.perform(post("/wechat/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": "wx-user-001",
                                  "message": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("message不能为空"))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }
}
