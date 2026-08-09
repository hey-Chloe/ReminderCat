package com.remindercat;

import com.remindercat.dto.TaskCreateRequest;
import com.remindercat.entity.Task;
import com.remindercat.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerTests {

    private final MockMvc mockMvc;
    private final TaskService taskService;

    @Autowired
    TaskControllerTests(MockMvc mockMvc, TaskService taskService) {
        this.mockMvc = mockMvc;
        this.taskService = taskService;
    }

    @Test
    void createTaskShouldSucceed() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": "task-create-user",
                                  "content": "下午开会",
                                  "remindTime": "2030-01-02T15:00:00"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.userId").value("task-create-user"))
                .andExpect(jsonPath("$.data.content").value("下午开会"))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void getTasksByUserIdShouldSucceed() throws Exception {
        taskService.createTask(createRequest("task-query-user", "查询测试"));

        mockMvc.perform(get("/api/tasks/task-query-user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].userId").value("task-query-user"))
                .andExpect(jsonPath("$.data[0].content").value("查询测试"))
                .andExpect(jsonPath("$.data[0].status").value("PENDING"));
    }

    @Test
    void completeTaskShouldSucceed() throws Exception {
        Task task = taskService.createTask(createRequest("task-complete-user", "完成测试"));

        mockMvc.perform(put("/api/tasks/{id}/complete", task.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(task.getId()))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }

    @Test
    void completeTaskShouldReturnErrorWhenTaskDoesNotExist() throws Exception {
        mockMvc.perform(put("/api/tasks/{id}/complete", Long.MAX_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("任务不存在"))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    @Test
    void createTaskShouldRejectInvalidParameters() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": "",
                                  "content": "",
                                  "remindTime": null
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    private TaskCreateRequest createRequest(String userId, String content) {
        TaskCreateRequest request = new TaskCreateRequest();
        request.setUserId(userId);
        request.setContent(content);
        request.setRemindTime(LocalDateTime.of(2030, 1, 2, 15, 0));
        return request;
    }
}
