package com.remindercat.dto;

import com.remindercat.entity.Task;
import com.remindercat.entity.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class TaskResponse {

    private final Long id;

    private final String userId;

    private final String content;

    private final LocalDateTime remindTime;

    private final TaskStatus status;

    public static TaskResponse from(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .userId(task.getUserId())
                .content(task.getContent())
                .remindTime(task.getRemindTime())
                .status(task.getStatus())
                .build();
    }
}
