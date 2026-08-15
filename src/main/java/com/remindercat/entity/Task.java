package com.remindercat.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    /** 任务临时唯一标识。 */
    private Long id;

    /** 创建任务的用户标识。 */
    private String userId;

    /** 提醒内容。 */
    private String content;

    /** 计划提醒时间。 */
    private LocalDateTime remindTime;

    /** 当前任务状态。 */
    private TaskStatus status;

    /** 任务创建时间。 */
    private LocalDateTime createdTime;

    /** 已尝试派发次数（claim 时递增）。 */
    private Integer retryCount;

    /** 下次重试时间，为空表示按 remind_time 首次派发。 */
    private LocalDateTime nextRetryTime;

    /** 完成时间。 */
    private LocalDateTime completedTime;

    /** 最近一次状态更新时间。 */
    private LocalDateTime updatedTime;
}
