package com.remindercat.service;

import com.remindercat.common.exception.BusinessException;
import com.remindercat.dto.TaskCreateRequest;
import com.remindercat.entity.Task;
import com.remindercat.entity.TaskStatus;
import com.remindercat.repository.TaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class TaskService {

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
}
