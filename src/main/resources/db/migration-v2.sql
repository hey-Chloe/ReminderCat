-- ReminderCat 迁移 v2：任务可靠性字段 + 企业微信消息去重表
-- 适用：已部署的 v1 数据库（新部署由 schema.sql 自动初始化，语句同样幂等）
--
-- 执行示例：
--   docker exec -i remindercat-postgres psql -U postgres -d remindercat < migration-v2.sql
--   或：psql -h <host> -U postgres -d remindercat -f migration-v2.sql

ALTER TABLE tasks ADD COLUMN IF NOT EXISTS retry_count INT NOT NULL DEFAULT 0;
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS next_retry_time TIMESTAMP NULL;
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS completed_time TIMESTAMP NULL;
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS updated_time TIMESTAMP NOT NULL DEFAULT NOW();

CREATE INDEX IF NOT EXISTS idx_tasks_processing_recovery ON tasks (status, updated_time);

CREATE TABLE IF NOT EXISTS wechat_messages (
    msg_id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(128) NOT NULL,
    msg_type VARCHAR(32) NOT NULL,
    received_time TIMESTAMP NOT NULL
);
