# Nexus Repository — 制品仓库

## 简介
Nexus 统一管理 Maven、npm、Docker 等制品，缓存公网依赖加速构建，存储内部发布包。

## 快速启动
```bash
docker compose up -d nexus
```
- 访问：`http://<IP>:8082`
- 初始密码：`docker exec nexus cat /nexus-data/admin.password`

## 本目录放置内容
- `maven-settings.xml` — Maven 客户端配置（指向 Nexus 代理）
- `cleanup-policies/` — 清理策略配置

## Maven 项目配置示例
```xml
<!-- settings.xml 片段 -->
<mirrors>
  <mirror>
    <id>nexus</id>
    <mirrorOf>*</mirrorOf>
    <url>http://<IP>:8082/repository/maven-public/</url>
  </mirror>
</mirrors>
```

## 官方文档
https://help.sonatype.com/repomanager3
