-- 去除 topic_library 表的 teacher_id 字段
-- 请逐条执行以下SQL语句

-- 第1条：删除外键约束（如果存在）
-- 先查询外键名称
SELECT CONSTRAINT_NAME 
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
WHERE TABLE_NAME = 'topic_library' 
AND COLUMN_NAME = 'teacher_id';

-- 第2条：删除外键约束（将 fk_topic_teacher 替换为上面查询到的实际外键名）
-- ALTER TABLE topic_library DROP FOREIGN KEY fk_topic_teacher;

-- 第3条：删除 teacher_id 字段
ALTER TABLE topic_library DROP COLUMN teacher_id;
