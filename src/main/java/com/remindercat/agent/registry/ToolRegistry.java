package com.remindercat.agent.registry;

import com.remindercat.agent.Intent;
import com.remindercat.agent.tool.AgentTool;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ToolRegistry {

    private final Map<Intent, AgentTool> tools = new ConcurrentHashMap<>();

    public ToolRegistry(List<AgentTool> agentTools) {
        agentTools.forEach(this::registerSupportedIntents);
    }

    public void register(Intent intent, AgentTool tool) {
        Objects.requireNonNull(intent, "intent不能为空");
        Objects.requireNonNull(tool, "tool不能为空");

        AgentTool existingTool = tools.putIfAbsent(intent, tool);
        if (existingTool != null && existingTool != tool) {
            throw new IllegalStateException("Intent已注册Tool: " + intent);
        }
    }

    public Optional<AgentTool> getTool(Intent intent) {
        if (intent == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(tools.get(intent));
    }

    private void registerSupportedIntents(AgentTool tool) {
        for (Intent intent : Intent.values()) {
            if (tool.supports(intent)) {
                register(intent, tool);
            }
        }
    }
}
