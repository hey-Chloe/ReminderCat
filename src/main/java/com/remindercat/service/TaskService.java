package com.remindercat.service;

import com.remindercat.common.exception.BusinessException;
import com.remindercat.dto.TaskCreateRequest;
import com.remindercat.entity.Task;
import com.remindercat.entity.TaskStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class TaskService {

    private final Map<Long, Task> taskStore = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong();

    public Task createTask(TaskCreateRequest request) {
        Long taskId = idGenerator.incrementAndGet();
        Task task = Task.builder()
                .id(taskId)
                .userId(request.getUserId())
                .content(request.getContent())
                .remindTime(request.getRemindTime())
                .status(TaskStatus.PENDING)
                .createdTime(LocalDateTime.now())
                .build();

        taskStore.put(taskId, task);
        log.info("Task created, taskId={}, userId={}", taskId, request.getUserId());
        return task;
    }

    public List<Task> getTasksByUserId(String userId) {
        return taskStore.values().stream()
                .filter(task -> task.getUserId().equals(userId))
                .sorted(Comparator.comparing(Task::getId))
                .toList();
    }

    public Task completeTask(Long taskId) {
        Task completedTask = taskStore.compute(taskId, (id, existingTask) -> {
            if (existingTask == null) {
                throw new BusinessException(404, "任务不存在");
            }

            return Task.builder()
                    .id(existingTask.getId())
                    .userId(existingTask.getUserId())
                    .content(existingTask.getContent())
                    .remindTime(existingTask.getRemindTime())
                    .status(TaskStatus.COMPLETED)
                    .createdTime(existingTask.getCreatedTime())
                    .build();
        });

        log.info("Task completed, taskId={}", taskId);
        return completedTask;
    }
}
