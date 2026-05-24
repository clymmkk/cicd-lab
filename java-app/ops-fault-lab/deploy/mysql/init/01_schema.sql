CREATE DATABASE IF NOT EXISTS fault_lab DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE fault_lab;

CREATE TABLE IF NOT EXISTS fault_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fault_type VARCHAR(50) NOT NULL COMMENT '故障类型',
    operator VARCHAR(50) DEFAULT 'ops_user',
    trigger_time DATETIME NOT NULL,
    recovery_time DATETIME NULL,
    jvm_metrics JSON NULL COMMENT '触发或恢复时的 JVM 快照',
    status ENUM('running', 'recovered', 'crashed') DEFAULT 'running'
);

CREATE TABLE IF NOT EXISTS fault_scenario (
    id INT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    level VARCHAR(20) NOT NULL,
    description VARCHAR(255) NOT NULL,
    scenario_code VARCHAR(50) NOT NULL UNIQUE,
    params JSON NULL COMMENT '默认参数',
    is_enabled BOOLEAN DEFAULT TRUE,
    sort_order INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user VARCHAR(50),
    action VARCHAR(200),
    ip VARCHAR(45),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO fault_scenario (id, name, level, description, scenario_code, params, is_enabled, sort_order)
VALUES
    (1, '堆内存溢出', '初级', '向静态列表持续添加 1MB 对象', 'heap-oom', JSON_OBJECT('blocks', 32), TRUE, 10),
    (2, '栈内存溢出', '初级', '无出口递归导致 StackOverflowError', 'stack-oom', JSON_OBJECT(), TRUE, 20),
    (3, 'CPU 飙高', '初级', '通过忙循环打满单核 CPU', 'cpu-busy', JSON_OBJECT(), TRUE, 30),
    (4, '死锁', '中级', '两个线程反向争抢锁', 'deadlock', JSON_OBJECT(), TRUE, 40),
    (5, '线程池耗尽', '中级', '固定线程池 + 慢任务堆积', 'thread-pool-exhaustion', JSON_OBJECT('tasks', 20, 'sleepSeconds', 30), TRUE, 50),
    (6, '频繁 Full GC', '中级', '持续分配大对象并持有强引用', 'full-gc', JSON_OBJECT('retainLimit', 8), TRUE, 60),
    (7, '数据库连接池耗尽', '高级', '慢查询占住连接池资源', 'db-pool-exhaustion', JSON_OBJECT('connections', 5, 'sleepSeconds', 10), TRUE, 70),
    (8, 'Redis 连接超时 / 慢查询', '高级', '执行慢命令制造阻塞', 'redis-latency', JSON_OBJECT('sleepSeconds', 5), TRUE, 80)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    level = VALUES(level),
    description = VALUES(description),
    params = VALUES(params),
    is_enabled = VALUES(is_enabled),
    sort_order = VALUES(sort_order);
