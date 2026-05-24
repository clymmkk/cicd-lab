# Ops Monitor - 运维监控平台

前后端分离的运维管理平台，支持服务器状态监控（CPU、内存、磁盘）、JWT 登录认证、服务器管理等功能。

## 技术栈

- **后端**：Spring Boot 3.2 + JDK 17 + Maven + JPA/Hibernate + Spring Security + JWT + OSHI
- **前端**：Vue 3 + Vite + Pinia + Axios + ECharts
- **数据库**：MySQL 8.0+

## 项目结构

```
ops-monitor/
├── pom.xml                          # Maven 依赖配置
├── src/main/java/com/ops/monitor/   # 后端代码
├── src/main/resources/
│   ├── application.yml              # 应用配置（MySQL、JPA、JWT）
│   └── schema.sql                  # 数据库初始化脚本
└── frontend/                       # Vue 3 前端
    ├── package.json
    ├── vite.config.js
    └── src/                         # 前端代码
```

## 快速开始

### 1. 数据库准备

确保本地已安装 MySQL 8.0+，执行：

```bash
mysql -u root -p < src/main/resources/schema.sql
```

### 2. 后端启动

```bash
# 编译并运行
mvn spring-boot:run
```

后端服务默认运行在 `http://localhost:8080`

### 3. 前端启动

```bash
cd frontend
npm install
npm run dev
```

前端开发服务器默认运行在 `http://localhost:5173`

## 功能特性

- **登录认证**：基于 JWT Token 的登录认证，前后端分离
- **监控仪表盘**：实时展示服务器 CPU、内存、磁盘使用率（ECharts 仪表盘）
- **服务器管理**：增删查服务器列表
- **定时采集**：每分钟自动采集一次本机监控数据并存入数据库

## API 接口

| 方法 | 接口 | 说明 |
|------|------|------|
| POST | `/api/auth/login` | 用户登录，返回 JWT Token |
| GET | `/api/servers` | 查询服务器列表 |
| POST | `/api/servers` | 新增服务器 |
| DELETE | `/api/servers/{id}` | 删除服务器 |
| GET | `/api/metrics/current` | 获取本机实时监控数据 |
| GET | `/api/metrics/{serverId}` | 查询指定服务器历史监控数据 |
| GET | `/api/metrics/{serverId}/latest` | 查询指定服务器最新监控数据 |

## 默认账号

- 用户名：`admin`
- 密码：`admin`

## 配置说明

`src/main/resources/application.yml` 中的核心配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ops_monitor
    username: root
    password: root

jwt:
  secret: ops-monitor-jwt-secret-key-2024-change-in-production
  expiration: 86400000
```

如需修改数据库连接或 JWT 密钥，直接编辑 `application.yml` 即可。
