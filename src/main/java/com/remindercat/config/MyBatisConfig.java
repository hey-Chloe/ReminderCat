package com.remindercat.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.remindercat.repository")
public class MyBatisConfig {
}
