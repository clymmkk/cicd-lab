# Ops Fault Lab

一个面向运维工程师的 Java 故障演练平台，采用前后端分离架构，聚焦 JVM、并发、数据库与 Redis 故障排查训练。

## 项目结构

```text
ops-fault-lab/
├── backend/                 # Spring Boot 3.5 + MyBatis Plus + Swagger
├── frontend/                # Vue 3 + Element Plus + ECharts
├── deploy/                  # MySQL 初始化 SQL 等部署材料
├── docs/                    # Postman / 说明文档扩展目录
├── docker-compose.yml
├── start.sh
├── TROUBLESHOOTING.md
├── PRINCIPLE.md
├── HOMEWORK.md
└── README.md
```

## 技术方案

- 后端：Spring Boot 3.5.0、JDK 17、MyBatis Plus 3.5.7
- 数据库：MySQL 8.0
- 缓存模拟：Redis 7
- 前端：Vue 3、Element Plus、Axios、ECharts
- 可观测：Spring Boot Actuator、JVM 快照接口
- 容器化：Docker、Docker Compose

## 故障场景

- 初级：堆内存溢出、栈内存溢出、CPU 飙高
- 中级：死锁、线程池耗尽、频繁 Full GC
- 高级：数据库连接池耗尽、Redis 连接超时 / 慢查询

## 本地开发

### 后端

```bash
cd backend
mvn clean package
mvn spring-boot:run
```

Swagger 地址：

```text
http://localhost:8080/swagger-ui/index.html
```

Actuator 地址：

```text
http://localhost:8080/actuator
```

### 前端

```bash
cd frontend
npm install
npm run dev
```

前端访问地址：

```text
http://localhost:5173
```

## Docker Compose 一键启动

```bash
docker compose up -d --build
```

启动后访问：

- 前端：http://localhost:5173
- 后端 Swagger：http://localhost:8080/swagger-ui/index.html
- MySQL：`localhost:3306`
- Redis：`localhost:6379`

## Docker 离线部署说明

需要先在可联网环境构建镜像，并将压缩后的镜像文件传输到目标服务器。

```bash
# 构建镜像
docker build -t ops-fault-lab-backend:1.0.0 ./backend
docker build -t ops-fault-lab-frontend:1.0.0 ./frontend

# 通用打包压缩
docker save ops-fault-lab-backend:1.0.0 | gzip > ops-fault-lab-backend.tar.gz
docker save ops-fault-lab-frontend:1.0.0 | gzip > ops-fault-lab-frontend.tar.gz
docker save mysql:8.0.39 | gzip > mysql-8.0.39.tar.gz
docker save redis:7.2-alpine | gzip > redis-7.2-alpine.tar.gz
```

在离线服务器执行：

```bash
# 通用解压导入
gunzip -c ops-fault-lab-backend.tar.gz | docker load
gunzip -c ops-fault-lab-frontend.tar.gz | docker load
gunzip -c mysql-8.0.39.tar.gz | docker load
gunzip -c redis-7.2-alpine.tar.gz | docker load
```

然后将项目目录和压缩后的镜像文件一起传输到目标服务器，执行：

```bash
docker compose up -d
```

## 故障演练说明

1. 默认同时只允许一个故障场景运行，避免互相干扰。
2. `stack-oom` 和 `deadlock` 为高风险场景，界面会提示不可安全停止。
3. 建议在容器或测试环境中配合 `jstat`、`jstack`、`jmap`、Arthas 进行观察。

## API 调试

- Swagger：`/swagger-ui/index.html`
- OpenAPI：`/v3/api-docs`
- Postman 示例见 [docs/postman/fault-lab.postman_collection.json](docs/postman/fault-lab.postman_collection.json)
