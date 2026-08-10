package com.remindercat.agent;

import com.remindercat.agent.registry.ToolRegistry;
import org.springframework.stereotype.Component;

@Component
public class AgentRuntime {

    private final ToolRegistry toolRegistry;

    public AgentRuntime(ToolRegistry toolRegistry) {
        this.toolRegistry = toolRegistry;
    }

    public AgentResult<?> execute(AgentState state) {
        if (state == null) {
            return AgentResult.failure(Intent.UNKNOWN, "Agent状态不能为空");
        }

        Intent intent = recognizeIntent(state.getInput());
        state.setIntent(intent);

        if (intent == Intent.UNKNOWN) {
            return AgentResult.failure(Intent.UNKNOWN, "无法识别用户意图");
        }

        return toolRegistry.getTool(intent)
                .map(tool -> tool.execute(state))
                .orElseGet(() -> AgentResult.failure(intent, "未找到可执行工具"));
    }

    private Intent recognizeIntent(String input) {
        if (input == null || input.isBlank()) {
            return Intent.UNKNOWN;
        }

        if (input.contains("提醒") || input.contains("创建")) {
            return Intent.CREATE_TASK;
        }

        if (input.contains("查询")) {
            return Intent.QUERY_TASK;
        }

        return Intent.UNKNOWN;
    }
}
