package com.remindercat.agent;

import com.remindercat.agent.memory.MemoryService;
import com.remindercat.agent.parser.IntentParser;
import com.remindercat.agent.registry.ToolRegistry;
import com.remindercat.agent.schema.TaskIntent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AgentRuntime {

    private final ToolRegistry toolRegistry;
    private final IntentParser intentParser;
    private final MemoryService memoryService;

    public AgentRuntime(ToolRegistry toolRegistry, IntentParser intentParser) {
        this(toolRegistry, intentParser, null);
    }

    @Autowired
    public AgentRuntime(
            ToolRegistry toolRegistry,
            IntentParser intentParser,
            MemoryService memoryService
    ) {
        this.toolRegistry = toolRegistry;
        this.intentParser = intentParser;
        this.memoryService = memoryService;
    }

    public AgentResult<?> execute(AgentState state) {
        if (state == null) {
            return AgentResult.failure(Intent.UNKNOWN, "Agent状态不能为空");
        }

        rememberUserMessage(state);

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
            return rememberResult(
                    state,
                    AgentResult.failure(Intent.UNKNOWN, "无法识别用户意图")
            );
        }

        AgentResult<?> result = toolRegistry.getTool(intent)
                .map(tool -> tool.execute(state))
                .orElseGet(() -> AgentResult.failure(intent, "未找到可执行工具"));
        return rememberResult(state, result);
    }

    private void rememberUserMessage(AgentState state) {
        if (memoryService != null) {
            memoryService.saveUserMessage(state.getUserId(), state.getInput());
        }
    }

    private AgentResult<?> rememberResult(AgentState state, AgentResult<?> result) {
        if (memoryService != null) {
            memoryService.saveAssistantMessage(state.getUserId(), result.getMessage());
        }
        return result;
    }

}
