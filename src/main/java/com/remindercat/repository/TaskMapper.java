package com.remindercat.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.remindercat.entity.TaskEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskMapper extends BaseMapper<TaskEntity> {

    @Select("""
            SELECT id, user_id, content, remind_time, status, created_time
            FROM tasks
            WHERE user_id = #{userId}
            ORDER BY id
            """)
    List<TaskEntity> selectByUserId(@Param("userId") String userId);

    @Select("""
            SELECT id, user_id, content, remind_time, status, created_time
            FROM tasks
            WHERE status = 'PENDING' AND remind_time <= #{currentTime}
            ORDER BY id
            """)
    List<TaskEntity> selectPendingTasksDue(@Param("currentTime") LocalDateTime currentTime);

    @Update("""
            UPDATE tasks
            SET status = 'PROCESSING'
            WHERE id = #{taskId} AND status = 'PENDING'
            """)
    int claimPendingTask(@Param("taskId") Long taskId);

    @Update("""
            UPDATE tasks
            SET status = #{status}
            WHERE id = #{taskId}
            """)
    int updateStatus(@Param("taskId") Long taskId, @Param("status") String status);
}
