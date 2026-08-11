package com.remindercat.scheduler;

import com.remindercat.entity.Task;
import com.remindercat.service.TaskService;
import com.remindercat.wechat.WeChatClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component("reminderTaskScheduler")
public class TaskScheduler {

    private final TaskService taskService;
    private final WeChatClient weChatClient;

    public TaskScheduler(TaskService taskService, WeChatClient weChatClient) {
        this.taskService = taskService;
        this.weChatClient = weChatClient;
    }

    @Scheduled(cron = "0 * * * * *")
    public void scanDueTasks() {
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
                weChatClient.sendReminder(task.getUserId(), task.getContent());
                taskService.completeTask(task.getId());
            } catch (RuntimeException exception) {
                log.error("Reminder delivery failed, taskId={}", task.getId(), exception);
                try {
                    taskService.failTask(task.getId());
                } catch (RuntimeException statusException) {
                    log.error("Failed to mark reminder task as FAILED, taskId={}", task.getId(), statusException);
                }
            }
        }
    }
}
