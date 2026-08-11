package com.remindercat;

import com.remindercat.agent.AgentResult;
import com.remindercat.agent.Intent;
import com.remindercat.agent.schema.TaskIntent;
import com.remindercat.agent.tool.TaskTool;
import com.remindercat.dto.TaskCreateRequest;
import com.remindercat.dto.TaskResponse;
import com.remindercat.entity.Task;
import com.remindercat.entity.TaskStatus;
import com.remindercat.service.TaskService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TaskToolTests {

    @Test
    void shouldConvertTaskIntentAndCreateTask() {
        TaskService taskService = mock(TaskService.class);
        TaskTool taskTool = new TaskTool(taskService);
        LocalDateTime remindTime = LocalDateTime.of(2026, 8, 11, 15, 0);
        TaskIntent taskIntent = TaskIntent.builder()
                .intent(Intent.CREATE_TASK)
                .content("开会")
                .remindTime(remindTime)
                .build();
        Task createdTask = Task.builder()
                .id(1L)
                .userId("wx-user-001")
                .content("开会")
                .remindTime(remindTime)
                .status(TaskStatus.PENDING)
                .createdTime(LocalDateTime.now())
                .build();
        when(taskService.createTask(org.mockito.ArgumentMatchers.any(TaskCreateRequest.class)))
                .thenReturn(createdTask);

        AgentResult<TaskResponse> result = taskTool.createTask("wx-user-001", taskIntent);

        ArgumentCaptor<TaskCreateRequest> requestCaptor = ArgumentCaptor.forClass(TaskCreateRequest.class);
        verify(taskService).createTask(requestCaptor.capture());
        TaskCreateRequest request = requestCaptor.getValue();
        assertEquals("wx-user-001", request.getUserId());
        assertEquals("开会", request.getContent());
        assertEquals(remindTime, request.getRemindTime());
        assertTrue(result.isSuccess());
        assertEquals(TaskStatus.PENDING, result.getData().getStatus());
    }
}
