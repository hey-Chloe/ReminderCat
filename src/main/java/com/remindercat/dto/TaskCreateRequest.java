package com.remindercat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskCreateRequest {

    @NotBlank(message = "userId不能为空")
    private String userId;

    @NotBlank(message = "content不能为空")
    private String content;

    @NotNull(message = "remindTime不能为空")
    private LocalDateTime remindTime;
}
