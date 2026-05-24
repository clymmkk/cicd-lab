# JFrog Artifactory — 通用制品仓库

## 简介
Artifactory 是业界最成熟的通用制品仓库，支持 Maven、npm、Docker、PyPI、Go 等 30+ 种包格式，比 Nexus 功能更强大。

## 快速启动（OSS 免费版）
```bash
docker compose up -d artifactory
```
- 访问：`http://<IP>:8083`
- 默认账号：`admin` / `password`（首次登录需修改）

## 本目录放置内容
- `artifactory.config.xml` — 导出配置
- `storage/` — 存储配置
- `licenses/` — License 文件（Pro 版）

## Nexus vs Artifactory 对比

| 特性 | Nexus OSS | Artifactory OSS |
|------|-----------|-----------------|
| Maven 代理 | ✅ | ✅ |
| Docker Registry | ✅ | ✅ |
| npm 仓库 | ✅ | ✅ |
| PyPI 仓库 | ❌ | ✅ |
| Go 仓库 | ❌ | ✅ |
| 多仓库聚合（Virtual Repo） | 有限 | ✅ |
| 复制/同步（远程仓库） | ❌（Pro） | ❌（Pro） |
| 构建信息（Build Info） | ❌ | ✅ |
| Web UI 体验 | 一般 | 优秀 |

## Jenkins 集成
安装插件 `Artifactory`，Pipeline 中使用：
```groovy
stage('发布到 Artifactory') {
    steps {
        rtUpload(
            serverId: 'artifactory',
            spec: '''{
                "files": [{
                    "pattern": "target/*.jar",
                    "target": "libs-release-local/com/ops/monitor/"
                }]
            }'''
        )
    }
}
```

## 官方文档
https://jfrog.com/help/r/jfrog-artifactory-documentation
