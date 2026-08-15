package com.remindercat.service;

import com.remindercat.common.exception.BusinessException;
import com.remindercat.dto.TaskCreateRequest;
import com.remindercat.entity.Task;
import com.remindercat.entity.TaskStatus;
import com.remindercat.repository.TaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class TaskService {

    /** 单任务最大派发次数（含首次尝试）。 */
    static final int MAX_DELIVERY_ATTEMPTS = 3;

    private static final long BASE_RETRY_DELAY_SECONDS = 60;

    private static final long MAX_RETRY_DELAY_SECONDS = 15 * 60;

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public Task createTask(TaskCreateRequest request) {
        Task task = Task.builder()
                .userId(request.getUserId())
                .content(request.getContent())
                .remindTime(request.getRemindTime())
                .status(TaskStatus.PENDING)
                .createdTime(LocalDateTime.now())
                .build();

        Task createdTask = taskRepository.createTask(task);
        log.info("Task created, taskId={}, userId={}", createdTask.getId(), request.getUserId());
        return createdTask;
    }

    public List<Task> getTasksByUserId(String userId) {
        return taskRepository.getTasksByUser(userId);
    }

    public List<Task> getPendingTasksDue(LocalDateTime currentTime) {
        return taskRepository.getPendingTasksDue(currentTime);
    }

    public Task completeTask(Long taskId) {
        Task completedTask = taskRepository.completeTask(taskId);
        if (completedTask == null) {
            throw new BusinessException(404, "任务不存在");
        }

        log.info("Task completed, taskId={}", taskId);
        return completedTask;
    }

    public boolean claimTask(Long taskId) {
        return taskRepository.claimTask(taskId);
    }

    public Task failTask(Long taskId) {
        Task failedTask = taskRepository.failTask(taskId);
        if (failedTask == null) {
            throw new BusinessException(404, "任务不存在");
        }
        log.warn("Task marked failed, taskId={}", taskId);
        return failedTask;
    }

    /**
     * 发送失败后的处理：未达上限则按退避时间重试，已达上限则标记 FAILED。
     * retry_count 在 claim 时递增，代表已尝试次数。
     */
    public Task markDeliveryFailed(Long taskId) {
        Task task = taskRepository.findById(taskId);
        if (task == null) {
            throw new BusinessException(404, "任务不存在");
        }
        int attempts = task.getRetryCount() == null ? 0 : task.getRetryCount();
        if (attempts >= MAX_DELIVERY_ATTEMPTS) {
            Task failedTask = taskRepository.failTask(taskId);
            log.warn("Task delivery failed permanently, taskId={}, attempts={}", taskId, attempts);
            return failedTask;
        }
        LocalDateTime nextRetryTime = LocalDateTime.now().plusSeconds(retryDelaySeconds(attempts));
        taskRepository.scheduleRetry(taskId, nextRetryTime);
        log.warn("Task delivery failed, retry scheduled, taskId={}, attempts={}, nextRetryTime={}",
                taskId, attempts, nextRetryTime);
        return taskRepository.findById(taskId);
    }

    /** 恢复长时间卡在 PROCESSING 的任务（进程崩溃、发送超时等场景）。 */
    public int recoverStaleProcessing(Duration timeout) {
        LocalDateTime cutoffTime = LocalDateTime.now().minus(timeout);
        int recovered = taskRepository.recoverStaleProcessing(cutoffTime);
        if (recovered > 0) {
            log.warn("Recovered stale PROCESSING tasks, count={}", recovered);
        }
        return recovered;
    }

    /** 退避策略：60s、120s、240s……封顶 15 分钟。 */
    static long retryDelaySeconds(int attempts) {
        return Math.min(BASE_RETRY_DELAY_SECONDS * (1L << (attempts - 1)), MAX_RETRY_DELAY_SECONDS);
    }
}
