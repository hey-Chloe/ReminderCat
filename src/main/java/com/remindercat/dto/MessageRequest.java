package com.remindercat.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MessageRequest {

    @NotBlank(message = "userId不能为空")
    private String userId;

    @NotBlank(message = "message不能为空")
    private String message;
}
