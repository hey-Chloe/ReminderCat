package com.remindercat.agent.parser;

import com.remindercat.agent.schema.TaskIntent;

@FunctionalInterface
public interface IntentParser {

    TaskIntent parse(String input);
}
