package com.remindercat;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.remindercat.repository.TaskMapper;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class MyBatisPlusStartupTests {

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Autowired
    private TaskMapper taskMapper;

    @Test
    void shouldLoadMyBatisPlusAndTaskMapper() {
        assertThat(sqlSessionFactory.getConfiguration())
                .isInstanceOf(MybatisConfiguration.class);
        assertThat(taskMapper).isNotNull();
    }
}
