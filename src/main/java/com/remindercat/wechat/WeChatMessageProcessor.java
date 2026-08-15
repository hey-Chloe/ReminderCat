package com.remindercat.wechat;

import com.remindercat.agent.AgentResult;
import com.remindercat.agent.AgentRuntime;
import com.remindercat.agent.AgentState;
import com.remindercat.agent.Intent;
import com.remindercat.dto.TaskResponse;
import com.remindercat.entity.TaskStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
public class WeChatMessageProcessor {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final AgentRuntime agentRuntime;
    private final WeChatClient weChatClient;
    private final ThreadPoolTaskExecutor wechatMessageExecutor;

    public WeChatMessageProcessor(
            AgentRuntime agentRuntime,
            WeChatClient weChatClient,
            ThreadPoolTaskExecutor wechatMessageExecutor
    ) {
        this.agentRuntime = agentRuntime;
        this.weChatClient = weChatClient;
        this.wechatMessageExecutor = wechatMessageExecutor;
    }

    /**
     * 异步处理：立即返回，不阻塞企业微信回调；
     * 结果通过企业微信主动推送回执给用户。
     */
    public void processAsync(AgentState state) {
        wechatMessageExecutor.execute(() -> process(state));
    }

    /** 同步处理（测试可直接调用）：意图解析 → Agent 执行 → 回执用户。 */
    public void process(AgentState state) {
        if (state == null || state.getUserId() == null || state.getUserId().isBlank()) {
            log.warn("忽略缺少 userId 的微信消息");
            return;
        }

        try {
            AgentResult<?> result = agentRuntime.execute(state);
            sendReply(state.getUserId(), buildReply(result));
        } catch (RuntimeException exception) {
            log.error("处理企业微信消息失败, userId={}", state.getUserId(), exception);
            sendReply(state.getUserId(), "处理失败，请稍后再试");
        }
    }

    private String buildReply(AgentResult<?> result) {
        if (result == null) {
            return "处理失败，请稍后再试";
        }
        if (!result.isSuccess()) {
            return switch (result.getIntent()) {
                case CREATE_TASK -> "提醒创建失败：" + result.getMessage();
                case QUERY_TASK -> "查询失败：" + result.getMessage();
                default -> result.getMessage();
            };
        }
        return switch (result.getIntent()) {
            case CREATE_TASK -> buildCreateReply((TaskResponse) result.getData());
            case QUERY_TASK -> buildQueryReply(result.getData());
            default -> result.getMessage();
        };
    }

    private String buildCreateReply(TaskResponse task) {
        if (task == null) {
            return "提醒创建失败：未返回任务信息";
        }
        return "已创建提醒 ✅\n内容：" + task.getContent()
                + "\n时间：" + task.getRemindTime().format(TIME_FORMAT);
    }

    private String buildQueryReply(Object data) {
        if (!(data instanceof List<?> tasks) || tasks.isEmpty()) {
            return "你还没有提醒任务哦";
        }
        StringBuilder builder = new StringBuilder("你共有 " + tasks.size() + " 条提醒：");
        int index = 1;
        for (Object item : tasks) {
            if (item instanceof TaskResponse task) {
                builder.append("\n").append(index++).append(". [")
                        .append(task.getRemindTime().format(TIME_FORMAT)).append("] ")
                        .append(task.getContent()).append("（").append(statusLabel(task.getStatus())).append("）");
            }
        }
        return builder.toString();
    }

    private String statusLabel(TaskStatus status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case PENDING -> "待提醒";
            case PROCESSING -> "发送中";
            case COMPLETED -> "已完成";
            case FAILED -> "失败";
        };
    }

    private void sendReply(String userId, String text) {
        try {
            weChatClient.sendReminder(userId, text);
        } catch (RuntimeException exception) {
            log.error("回执发送失败, userId={}", userId, exception);
        }
    }
}
