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
            SELECT id, user_id, content, remind_time, status, created_time,
                   retry_count, next_retry_time, completed_time, updated_time
            FROM tasks
            WHERE user_id = #{userId}
            ORDER BY id
            """)
    List<TaskEntity> selectByUserId(@Param("userId") String userId);

    @Select("""
            SELECT id, user_id, content, remind_time, status, created_time,
                   retry_count, next_retry_time, completed_time, updated_time
            FROM tasks
            WHERE status = 'PENDING'
              AND remind_time <= #{currentTime}
              AND (next_retry_time IS NULL OR next_retry_time <= #{currentTime})
            ORDER BY id
            LIMIT 100
            """)
    List<TaskEntity> selectPendingTasksDue(@Param("currentTime") LocalDateTime currentTime);

    @Update("""
            UPDATE tasks
            SET status = 'PROCESSING', retry_count = retry_count + 1, updated_time = NOW()
            WHERE id = #{taskId} AND status = 'PENDING'
            """)
    int claimPendingTask(@Param("taskId") Long taskId);

    @Update("""
            UPDATE tasks
            SET status = #{status},
                completed_time = CASE WHEN #{status} = 'COMPLETED' THEN NOW() ELSE completed_time END,
                updated_time = NOW()
            WHERE id = #{taskId}
            """)
    int updateStatus(@Param("taskId") Long taskId, @Param("status") String status);

    @Update("""
            UPDATE tasks
            SET status = 'PENDING', next_retry_time = #{nextRetryTime}, updated_time = NOW()
            WHERE id = #{taskId}
            """)
    int scheduleRetry(@Param("taskId") Long taskId, @Param("nextRetryTime") LocalDateTime nextRetryTime);

    @Update("""
            UPDATE tasks
            SET status = 'PENDING', updated_time = NOW()
            WHERE status = 'PROCESSING' AND updated_time < #{cutoffTime}
            """)
    int recoverStaleProcessing(@Param("cutoffTime") LocalDateTime cutoffTime);
}
