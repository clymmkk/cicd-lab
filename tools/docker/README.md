# Docker — 容器化打包

## 简介
Docker 将应用及其依赖打包成镜像，保证开发/测试/生产环境一致性。

## 本目录放置内容
- `Dockerfile` — 各语言项目 Dockerfile 示例
- `docker-compose.yml` — 多服务编排示例
- `.dockerignore` — 构建排除规则
- `scripts/` — 镜像构建/分发脚本

## 多阶段构建示例（Java）
```dockerfile
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## 离线镜像分发
```bash
# 在线机器导出
docker save ops-monitor:1.0 | gzip > ops-monitor.tar.gz

# 离线服务器加载
gunzip -c ops-monitor.tar.gz | docker load
```

## 官方文档
https://docs.docker.com/
