package com.remindercat.agent;

import com.remindercat.agent.parser.IntentParser;
import com.remindercat.agent.registry.ToolRegistry;
import com.remindercat.agent.schema.TaskIntent;
import org.springframework.stereotype.Component;

@Component
public class AgentRuntime {

    private final ToolRegistry toolRegistry;
    private final IntentParser intentParser;

    public AgentRuntime(ToolRegistry toolRegistry, IntentParser intentParser) {
        this.toolRegistry = toolRegistry;
        this.intentParser = intentParser;
    }

    public AgentResult<?> execute(AgentState state) {
        if (state == null) {
            return AgentResult.failure(Intent.UNKNOWN, "Agent状态不能为空");
        }

        TaskIntent taskIntent = intentParser.parse(state.getInput());
        Intent intent = taskIntent == null || taskIntent.getIntent() == null
                ? Intent.UNKNOWN
                : taskIntent.getIntent();
        state.setIntent(intent);
        if (taskIntent != null && taskIntent.getContent() != null) {
            state.setContent(taskIntent.getContent());
        }
        if (taskIntent != null && taskIntent.getRemindTime() != null) {
            state.setRemindTime(taskIntent.getRemindTime());
        }

        if (intent == Intent.UNKNOWN) {
            return AgentResult.failure(Intent.UNKNOWN, "无法识别用户意图");
        }

        return toolRegistry.getTool(intent)
                .map(tool -> tool.execute(state))
                .orElseGet(() -> AgentResult.failure(intent, "未找到可执行工具"));
    }

}
