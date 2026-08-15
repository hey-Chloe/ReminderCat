package com.remindercat.service;

import com.remindercat.entity.Task;
import com.remindercat.entity.TaskStatus;
import com.remindercat.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TaskServiceRetryTests {

    private final TaskRepository taskRepository = mock(TaskRepository.class);

    private final TaskService taskService = new TaskService(taskRepository);

    @Test
    void shouldScheduleBackoffRetryWhenAttemptsBelowMax() {
        when(taskRepository.findById(1L)).thenReturn(taskWithRetryCount(1));

        taskService.markDeliveryFailed(1L);

        ArgumentCaptor<LocalDateTime> nextRetryCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(taskRepository).scheduleRetry(eq(1L), nextRetryCaptor.capture());
        verify(taskRepository, never()).failTask(1L);

        LocalDateTime nextRetryTime = nextRetryCaptor.getValue();
        assertThat(nextRetryTime)
                .isAfterOrEqualTo(LocalDateTime.now().plusSeconds(59))
                .isBeforeOrEqualTo(LocalDateTime.now().plusSeconds(61));
    }

    @Test
    void shouldMarkFailedWhenAttemptsReachMax() {
        when(taskRepository.findById(1L))
                .thenReturn(taskWithRetryCount(TaskService.MAX_DELIVERY_ATTEMPTS));

        taskService.markDeliveryFailed(1L);

        verify(taskRepository).failTask(1L);
        verify(taskRepository, never()).scheduleRetry(any(), any());
    }

    @Test
    void retryDelayShouldGrowWithAttemptsAndCapAtFifteenMinutes() {
        assertThat(TaskService.retryDelaySeconds(1)).isEqualTo(60);
        assertThat(TaskService.retryDelaySeconds(2)).isEqualTo(120);
        assertThat(TaskService.retryDelaySeconds(3)).isEqualTo(240);
        assertThat(TaskService.retryDelaySeconds(10)).isEqualTo(900);
    }

    private Task taskWithRetryCount(int retryCount) {
        return Task.builder()
                .id(1L)
                .userId("user-1")
                .content("提醒内容")
                .remindTime(LocalDateTime.now().plusMinutes(1))
                .status(TaskStatus.PROCESSING)
                .retryCount(retryCount)
                .createdTime(LocalDateTime.now())
                .updatedTime(LocalDateTime.now())
                .build();
    }
}
