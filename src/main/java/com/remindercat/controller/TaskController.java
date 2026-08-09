package com.remindercat.controller;

import com.remindercat.common.response.Result;
import com.remindercat.dto.TaskCreateRequest;
import com.remindercat.dto.TaskResponse;
import com.remindercat.entity.Task;
import com.remindercat.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public Result<TaskResponse> createTask(@Valid @RequestBody TaskCreateRequest request) {
        Task task = taskService.createTask(request);
        return Result.success(TaskResponse.from(task));
    }

    @GetMapping("/{userId}")
    public Result<List<TaskResponse>> getTasks(@PathVariable String userId) {
        List<TaskResponse> tasks = taskService.getTasksByUserId(userId).stream()
                .map(TaskResponse::from)
                .toList();

        return Result.success(tasks);
    }

    @PutMapping("/{id}/complete")
    public Result<TaskResponse> completeTask(@PathVariable Long id) {
        Task task = taskService.completeTask(id);
        return Result.success(TaskResponse.from(task));
    }
}
