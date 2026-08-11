package com.remindercat.repository;

import com.remindercat.entity.Task;
import com.remindercat.entity.TaskEntity;
import com.remindercat.entity.TaskStatus;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class TaskRepository {

    private final TaskMapper taskMapper;

    public TaskRepository(TaskMapper taskMapper) {
        this.taskMapper = taskMapper;
    }

    public Task createTask(Task task) {
        TaskEntity entity = toEntity(task);
        taskMapper.insert(entity);
        return toDomain(entity);
    }

    public List<Task> getTasksByUser(String userId) {
        return taskMapper.selectByUserId(userId).stream()
                .map(this::toDomain)
                .toList();
    }

    public List<Task> getPendingTasksDue(LocalDateTime currentTime) {
        return taskMapper.selectPendingTasksDue(currentTime).stream()
                .map(this::toDomain)
                .toList();
    }

    public boolean claimTask(Long taskId) {
        return taskMapper.claimPendingTask(taskId) == 1;
    }

    public Task completeTask(Long taskId) {
        return updateTaskStatus(taskId, TaskStatus.COMPLETED);
    }

    public Task failTask(Long taskId) {
        return updateTaskStatus(taskId, TaskStatus.FAILED);
    }

    public Task findById(Long taskId) {
        TaskEntity entity = taskMapper.selectById(taskId);
        return entity == null ? null : toDomain(entity);
    }

    private Task updateTaskStatus(Long taskId, TaskStatus status) {
        if (taskMapper.updateStatus(taskId, status.name()) == 0) {
            return null;
        }
        return findById(taskId);
    }

    private TaskEntity toEntity(Task task) {
        return TaskEntity.builder()
                .id(task.getId())
                .userId(task.getUserId())
                .content(task.getContent())
                .remindTime(task.getRemindTime())
                .status(task.getStatus())
                .createdTime(task.getCreatedTime())
                .build();
    }

    private Task toDomain(TaskEntity entity) {
        return Task.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .content(entity.getContent())
                .remindTime(entity.getRemindTime())
                .status(entity.getStatus())
                .createdTime(entity.getCreatedTime())
                .build();
    }
}
