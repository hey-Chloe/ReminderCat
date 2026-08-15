package com.remindercat.scheduler;

import com.remindercat.entity.Task;
import com.remindercat.service.TaskService;
import com.remindercat.wechat.WeChatClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component("reminderTaskScheduler")
@ConditionalOnProperty(name = "remindercat.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class TaskScheduler {

    /** PROCESSING 超过该时长未完成即视为僵死，恢复为 PENDING 重新派发。 */
    private static final Duration PROCESSING_TIMEOUT = Duration.ofMinutes(5);

    private final TaskService taskService;
    private final WeChatClient weChatClient;

    public TaskScheduler(TaskService taskService, WeChatClient weChatClient) {
        this.taskService = taskService;
        this.weChatClient = weChatClient;
    }

    @Scheduled(fixedDelay = 30_000)
    public void scanDueTasks() {
        taskService.recoverStaleProcessing(PROCESSING_TIMEOUT);

        List<Task> dueTasks = taskService.getPendingTasksDue(LocalDateTime.now());
        if (!dueTasks.isEmpty()) {
            log.info("Due reminder tasks found, count={}", dueTasks.size());
        }

        for (Task task : dueTasks) {
            if (!taskService.claimTask(task.getId())) {
                log.debug("Task claim skipped, taskId={}", task.getId());
                continue;
            }

            try {
                weChatClient.sendReminder(
                        task.getUserId(),
                        "🔔 提醒喵：" + task.getContent()
                );
                taskService.completeTask(task.getId());
            } catch (RuntimeException exception) {
                int attempts = (task.getRetryCount() == null ? 0 : task.getRetryCount()) + 1;
                log.error("Reminder delivery failed, taskId={}, attempts={}", task.getId(), attempts, exception);
                try {
                    taskService.markDeliveryFailed(task.getId());
                } catch (RuntimeException statusException) {
                    log.error("Failed to update retry status, taskId={}", task.getId(), statusException);
                }
            }
        }
    }
}
