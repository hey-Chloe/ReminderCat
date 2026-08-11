package com.remindercat;

import com.remindercat.entity.Task;
import com.remindercat.entity.TaskStatus;
import com.remindercat.scheduler.TaskScheduler;
import com.remindercat.service.TaskService;
import com.remindercat.wechat.WeChatClient;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TaskSchedulerTests {

    @Test
    void shouldClaimSendAndCompleteDueTask() {
        TaskService taskService = mock(TaskService.class);
        WeChatClient weChatClient = mock(WeChatClient.class);
        TaskScheduler scheduler = new TaskScheduler(taskService, weChatClient);
        Task dueTask = task(1L, "due-user", "到期开会提醒");
        when(taskService.getPendingTasksDue(any(LocalDateTime.class))).thenReturn(List.of(dueTask));
        when(taskService.claimTask(1L)).thenReturn(true);

        scheduler.scanDueTasks();

        verify(weChatClient).sendReminder("due-user", "到期开会提醒");
        verify(taskService).completeTask(1L);
        verify(taskService, never()).failTask(1L);
    }

    @Test
    void shouldMarkClaimedTaskFailedWhenSendingFails() {
        TaskService taskService = mock(TaskService.class);
        WeChatClient weChatClient = mock(WeChatClient.class);
        TaskScheduler scheduler = new TaskScheduler(taskService, weChatClient);
        Task dueTask = task(2L, "failed-user", "失败提醒");
        when(taskService.getPendingTasksDue(any(LocalDateTime.class))).thenReturn(List.of(dueTask));
        when(taskService.claimTask(2L)).thenReturn(true);
        doThrow(new RuntimeException("send failed"))
                .when(weChatClient).sendReminder("failed-user", "失败提醒");

        scheduler.scanDueTasks();

        verify(taskService).failTask(2L);
        verify(taskService, never()).completeTask(2L);
    }

    @Test
    void shouldSkipTaskWhenAtomicClaimFails() {
        TaskService taskService = mock(TaskService.class);
        WeChatClient weChatClient = mock(WeChatClient.class);
        TaskScheduler scheduler = new TaskScheduler(taskService, weChatClient);
        Task dueTask = task(3L, "claimed-user", "已被抢占提醒");
        when(taskService.getPendingTasksDue(any(LocalDateTime.class))).thenReturn(List.of(dueTask));
        when(taskService.claimTask(3L)).thenReturn(false);

        scheduler.scanDueTasks();

        verify(weChatClient, never()).sendReminder(any(), any());
        verify(taskService, never()).completeTask(3L);
        verify(taskService, never()).failTask(3L);
    }

    private Task task(Long id, String userId, String content) {
        return Task.builder()
                .id(id)
                .userId(userId)
                .content(content)
                .remindTime(LocalDateTime.now().minusMinutes(1))
                .status(TaskStatus.PENDING)
                .createdTime(LocalDateTime.now().minusHours(1))
                .build();
    }
}
