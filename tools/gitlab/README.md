# GitLab CE — 代码托管

## 简介
GitLab 是一个完整的 DevOps 平台，这里主要使用其代码托管和 MR 协作功能。

## 快速启动
```bash
docker compose up -d gitlab
```
- 访问：`http://<IP>:8929`
- SSH：端口 `8930`
- 初始密码：`docker exec -it gitlab grep 'Password:' /etc/gitlab/initial_root_password`

## 本目录放置内容
- GitLab 备份脚本
- 自定义配置片段
- CI/CD Runner 配置

## 常用操作
```bash
# 备份
docker exec gitlab gitlab-backup create

# 重新配置
docker exec gitlab gitlab-ctl reconfigure

# 查看日志
docker logs -f gitlab
```

## 官方文档
https://docs.gitlab.com/
