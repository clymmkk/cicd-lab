-- 运维监控平台数据库初始化脚本
-- 创建于 2024-05-24

CREATE DATABASE IF NOT EXISTS ops_monitor
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE ops_monitor;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 服务器信息表
CREATE TABLE IF NOT EXISTS servers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    host VARCHAR(100) NOT NULL,
    os VARCHAR(50),
    status VARCHAR(20) DEFAULT 'ONLINE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 服务器监控数据表
CREATE TABLE IF NOT EXISTS server_metrics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    server_id BIGINT NOT NULL,
    cpu_usage DOUBLE,
    memory_used BIGINT,
    memory_total BIGINT,
    disk_used BIGINT,
    disk_total BIGINT,
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (server_id) REFERENCES servers(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 插入默认管理员用户 (密码: admin，BCrypt 加密)
INSERT IGNORE INTO users (username, password, role) VALUES
('admin', '$2a$10$N.zJ5dR7PzWfwB8s6C9u.OF0z9z5z5z5z5z5z5z5z5z5z5z5z5z5z5z', 'ADMIN');
