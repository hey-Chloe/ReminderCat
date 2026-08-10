package com.remindercat;

import com.remindercat.agent.AgentResult;
import com.remindercat.agent.AgentRuntime;
import com.remindercat.agent.AgentState;
import com.remindercat.agent.Intent;
import com.remindercat.agent.registry.ToolRegistry;
import com.remindercat.agent.tool.AgentTool;
import com.remindercat.agent.tool.TaskTool;
import com.remindercat.dto.TaskResponse;
import com.remindercat.service.TaskService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ToolRegistryTests {

    @Test
    void createTaskShouldFindAndExecuteRegisteredTaskTool() {
        TaskTool taskTool = new TaskTool(new TaskService());
        ToolRegistry toolRegistry = new ToolRegistry(List.of());
        toolRegistry.register(Intent.CREATE_TASK, taskTool);
        AgentRuntime agentRuntime = new AgentRuntime(toolRegistry);
        AgentState state = AgentState.builder()
                .userId("registry-create-user")
                .input("创建一个开会提醒")
                .remindTime(LocalDateTime.of(2030, 1, 2, 15, 0))
                .build();

        AgentTool registeredTool = toolRegistry.getTool(Intent.CREATE_TASK).orElseThrow();
        AgentResult<?> result = agentRuntime.execute(state);

        assertSame(taskTool, registeredTool);
        assertTrue(result.isSuccess());
        assertEquals(Intent.CREATE_TASK, result.getIntent());
        assertInstanceOf(TaskResponse.class, result.getData());
    }

    @Test
    void unknownIntentShouldNotResolveToolAndShouldReturnFailure() {
        TaskTool taskTool = new TaskTool(new TaskService());
        ToolRegistry toolRegistry = new ToolRegistry(List.of(taskTool));
        AgentRuntime agentRuntime = new AgentRuntime(toolRegistry);
        AgentState state = AgentState.builder()
                .userId("registry-unknown-user")
                .input("你好")
                .build();

        AgentResult<?> result = agentRuntime.execute(state);

        assertTrue(toolRegistry.getTool(Intent.UNKNOWN).isEmpty());
        assertFalse(result.isSuccess());
        assertEquals(Intent.UNKNOWN, result.getIntent());
        assertEquals("无法识别用户意图", result.getMessage());
    }
}
