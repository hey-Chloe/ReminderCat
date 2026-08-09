package com.remindercat.agent.tool;

import com.remindercat.agent.AgentResult;
import com.remindercat.agent.AgentState;
import com.remindercat.agent.Intent;
import com.remindercat.dto.TaskCreateRequest;
import com.remindercat.dto.TaskResponse;
import com.remindercat.entity.Task;
import com.remindercat.service.TaskService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TaskTool implements AgentTool {

    private final TaskService taskService;

    public TaskTool(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public boolean supports(Intent intent) {
        return intent == Intent.CREATE_TASK || intent == Intent.QUERY_TASK;
    }

    @Override
    public AgentResult<?> execute(AgentState state) {
        return switch (state.getIntent()) {
            case CREATE_TASK -> createTask(state);
            case QUERY_TASK -> queryTasks(state);
            case UNKNOWN -> AgentResult.failure(Intent.UNKNOWN, "无法识别用户意图");
        };
    }

    private AgentResult<TaskResponse> createTask(AgentState state) {
        if (state.getUserId() == null || state.getUserId().isBlank()) {
            return AgentResult.failure(Intent.CREATE_TASK, "userId不能为空");
        }

        if (state.getRemindTime() == null) {
            return AgentResult.failure(Intent.CREATE_TASK, "remindTime不能为空");
        }

        TaskCreateRequest request = new TaskCreateRequest();
        request.setUserId(state.getUserId());
        request.setContent(state.getInput());
        request.setRemindTime(state.getRemindTime());

        Task task = taskService.createTask(request);
        return AgentResult.success(Intent.CREATE_TASK, "任务创建成功", TaskResponse.from(task));
    }

    private AgentResult<List<TaskResponse>> queryTasks(AgentState state) {
        if (state.getUserId() == null || state.getUserId().isBlank()) {
            return AgentResult.failure(Intent.QUERY_TASK, "userId不能为空");
        }

        List<TaskResponse> tasks = taskService.getTasksByUserId(state.getUserId()).stream()
                .map(TaskResponse::from)
                .toList();

        return AgentResult.success(Intent.QUERY_TASK, "任务查询成功", tasks);
    }
}
