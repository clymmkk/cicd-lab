# Jenkins LTS — CI/CD 流水线

## 简介
Jenkins 是最广泛使用的开源 CI/CD 工具，支持 Pipeline as Code。

## 快速启动
```bash
docker compose up -d jenkins
```
- 访问：`http://<IP>:8081`
- 初始密码：`docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword`

## 本目录放置内容
- `Jenkinsfile` — 流水线定义
- `groovy/` — 共享库（Shared Library）
- `scripts/` — 构建/部署脚本

## 必装插件
- GitLab Plugin
- Pipeline
- Docker Pipeline
- SonarQube Scanner
- Publish Over SSH

## 官方文档
https://www.jenkins.io/doc/
