# SonarQube — 代码质量扫描

## 简介
SonarQube 自动检测代码中的 Bug、漏洞、代码异味和重复率。

## 快速启动
```bash
docker compose up -d sonarqube
```
- 访问：`http://<IP>:9000`
- 默认账号：`admin` / `admin`（首次登录需修改）

## 本目录放置内容
- `sonar-project.properties` — 项目扫描配置
- `quality-profiles/` — 自定义质量规则导出

## Jenkins 集成示例
```groovy
stage('SonarQube 扫描') {
    steps {
        sh 'mvn sonar:sonar -Dsonar.host.url=http://sonarqube:9000 -Dsonar.login=${SONAR_TOKEN}'
    }
}
```

## 官方文档
https://docs.sonarsource.com/sonarqube/
