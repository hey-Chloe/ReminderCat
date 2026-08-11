package com.remindercat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tasks")
public class TaskEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String userId;

    private String content;

    private LocalDateTime remindTime;

    private TaskStatus status;

    private LocalDateTime createdTime;
}
