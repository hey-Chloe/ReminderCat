package com.remindercat.agent.parser;

import com.remindercat.agent.Intent;
import com.remindercat.agent.schema.TaskIntent;
import org.springframework.stereotype.Component;

@Component
public class RuleBasedIntentParser implements IntentParser {

    @Override
    public TaskIntent parse(String input) {
        Intent intent;
        if (input == null || input.isBlank()) {
            intent = Intent.UNKNOWN;
        } else if (input.contains("提醒") || input.contains("创建")) {
            intent = Intent.CREATE_TASK;
        } else if (input.contains("查询")) {
            intent = Intent.QUERY_TASK;
        } else {
            intent = Intent.UNKNOWN;
        }

        return TaskIntent.builder()
                .intent(intent)
                .content(input)
                .build();
    }
}
