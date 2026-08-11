package com.remindercat;

import com.remindercat.agent.AgentResult;
import com.remindercat.agent.AgentRuntime;
import com.remindercat.agent.AgentState;
import com.remindercat.agent.Intent;
import com.remindercat.agent.parser.IntentParser;
import com.remindercat.agent.parser.RuleBasedIntentParser;
import com.remindercat.agent.registry.ToolRegistry;
import com.remindercat.agent.schema.TaskIntent;
import com.remindercat.agent.tool.TaskTool;
import com.remindercat.dto.TaskResponse;
import com.remindercat.entity.Task;
import com.remindercat.entity.TaskStatus;
import com.remindercat.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AgentRuntimeTests {

    private AgentRuntime agentRuntime;

    @BeforeEach
    void setUp() {
        TaskService taskService = taskServiceStub();
        TaskTool taskTool = new TaskTool(taskService);
        ToolRegistry toolRegistry = new ToolRegistry(List.of(taskTool));
        agentRuntime = new AgentRuntime(toolRegistry, new RuleBasedIntentParser());
    }

    @Test
    void shouldRecognizeCreateIntentAndExecuteTaskTool() {
        AgentState state = AgentState.builder()
                .userId("agent-create-user")
                .input("明天下午3点提醒我开会")
                .remindTime(LocalDateTime.of(2030, 1, 2, 15, 0))
                .build();

        AgentResult<?> result = agentRuntime.execute(state);

        assertTrue(result.isSuccess());
        assertEquals(Intent.CREATE_TASK, state.getIntent());
        assertEquals(Intent.CREATE_TASK, result.getIntent());
        assertInstanceOf(TaskResponse.class, result.getData());
        TaskResponse task = (TaskResponse) result.getData();
        assertEquals("agent-create-user", task.getUserId());
        assertEquals("明天下午3点提醒我开会", task.getContent());
        assertEquals("PENDING", task.getStatus().name());
    }

    @Test
    void shouldRecognizeCreateKeyword() {
        AgentState state = AgentState.builder()
                .userId("agent-keyword-user")
                .input("创建一个喝水任务")
                .remindTime(LocalDateTime.of(2030, 1, 2, 16, 0))
                .build();

        AgentResult<?> result = agentRuntime.execute(state);

        assertTrue(result.isSuccess());
        assertEquals(Intent.CREATE_TASK, result.getIntent());
    }

    @Test
    void shouldRecognizeQueryIntentAndExecuteTaskTool() {
        AgentState state = AgentState.builder()
                .userId("agent-query-user")
                .input("查询我的任务")
                .build();

        AgentResult<?> result = agentRuntime.execute(state);

        assertTrue(result.isSuccess());
        assertEquals(Intent.QUERY_TASK, state.getIntent());
        assertInstanceOf(List.class, result.getData());
        assertTrue(((List<?>) result.getData()).isEmpty());
    }

    @Test
    void shouldReturnFailureForUnknownIntent() {
        AgentState state = AgentState.builder()
                .userId("agent-unknown-user")
                .input("你好")
                .build();

        AgentResult<?> result = agentRuntime.execute(state);

        assertFalse(result.isSuccess());
        assertEquals(Intent.UNKNOWN, result.getIntent());
        assertEquals("无法识别用户意图", result.getMessage());
    }

    @Test
    void shouldReturnFailureWhenNoToolSupportsIntent() {
        AgentRuntime runtimeWithoutTools = new AgentRuntime(
                new ToolRegistry(List.of()),
                new RuleBasedIntentParser()
        );
        AgentState state = AgentState.builder()
                .userId("agent-no-tool-user")
                .input("查询我的任务")
                .build();

        AgentResult<?> result = runtimeWithoutTools.execute(state);

        assertFalse(result.isSuccess());
        assertEquals(Intent.QUERY_TASK, result.getIntent());
        assertEquals("未找到可执行工具", result.getMessage());
    }

    @Test
    void shouldAllowIntentParserToBeReplaced() {
        IntentParser fixedParser = input -> TaskIntent.builder()
                .intent(Intent.QUERY_TASK)
                .content(input)
                .build();
        TaskTool taskTool = new TaskTool(taskServiceStub());
        AgentRuntime runtime = new AgentRuntime(
                new ToolRegistry(List.of(taskTool)),
                fixedParser
        );
        AgentState state = AgentState.builder()
                .userId("replaceable-parser-user")
                .input("任意输入")
                .build();

        AgentResult<?> result = runtime.execute(state);

        assertTrue(result.isSuccess());
        assertEquals(Intent.QUERY_TASK, result.getIntent());
    }

    private TaskService taskServiceStub() {
        TaskService taskService = mock(TaskService.class);
        when(taskService.createTask(any())).thenAnswer(invocation -> {
            com.remindercat.dto.TaskCreateRequest request = invocation.getArgument(0);
            return Task.builder()
                    .id(1L)
                    .userId(request.getUserId())
                    .content(request.getContent())
                    .remindTime(request.getRemindTime())
                    .status(TaskStatus.PENDING)
                    .createdTime(LocalDateTime.now())
                    .build();
        });
        when(taskService.getTasksByUserId(any())).thenReturn(List.of());
        return taskService;
    }
}
