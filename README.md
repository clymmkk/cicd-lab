# CICD Lab — CI/CD 全工具链实验环境

> 从零搭建一套完整的企业级 CI/CD 环境，覆盖代码托管、自动构建、质量扫描、制品管理、容器化、自动化部署全链路。

---

## 工具链总览

```
┌───────────────────────────────────────────────────────┐
│  代码管理   GitLab（私有仓库 + MR 协作）               │
└─────────────────────────┬─────────────────────────────┘
                          ↓
┌───────────────────────────────────────────────────────┐
│  持续集成   Jenkins（流水线）+ SonarQube（代码质量）    │
└─────────────────────────┬─────────────────────────────┘
                          ↓
┌───────────────────────────────────────────────────────┐
│  制品管理   Nexus 或 Artifactory（Maven/npm/Docker）   │
└─────────────────────────┬─────────────────────────────┘
                          ↓
┌───────────────────────────────────────────────────────┐
│  容器化     Docker（镜像构建）+ Harbor（镜像仓库）      │
└─────────────────────────┬─────────────────────────────┘
                          ↓
┌───────────────────────────────────────────────────────┐
│  自动化部署  Ansible（配置管理）+ Nginx（反向代理）     │
└─────────────────────────┬─────────────────────────────┘
                          ↓
┌───────────────────────────────────────────────────────┐
│  进阶       Kubernetes（容器编排）+ ArgoCD（GitOps）   │
└───────────────────────────────────────────────────────┘
```

---

## 项目结构

```
cicd-lab/
├── tools/                 # CI/CD 工具链（每个工具一个目录）
│   ├── gitlab/            # 代码托管 - GitLab CE
│   ├── jenkins/           # CI/CD 流水线 - Jenkins LTS
│   ├── sonarqube/         # 代码质量扫描 - SonarQube
│   ├── nexus/             # 制品仓库 - Nexus Repository
│   ├── artifactory/       # 制品仓库 - JFrog Artifactory
│   ├── docker/            # 容器化打包 - Dockerfile 实践
│   ├── harbor/            # 镜像仓库 - Harbor
│   ├── ansible/           # 自动化部署 - Ansible
│   ├── nginx/             # 反向代理 - Nginx
│   ├── kubernetes/        # 容器编排 - Kubernetes（进阶）
│   └── argocd/            # GitOps - ArgoCD（进阶）
├── java-app/              # 示例项目 - Java 应用
├── docker-compose.yml     # 服务编排配置
└── README.md              # 本文档
```

---

## 环境要求

| 工具 | 版本 | 说明 |
|------|------|------|
| Docker | >= 20.10 | 容器引擎（必装） |
| Docker Compose | >= 2.0 | 多容器编排（必装） |
| Git | >= 2.30 | 版本控制 |

| 语言 | 版本 | 包管理 |
|------|------|--------|
| Java | JDK 17 | Maven >= 3.8 |
| Python | >= 3.9 | uv（推荐）/ pip |
| Go | >= 1.21 | Go Modules |

---

## 快速启动

```bash
# 一键启动所有服务
docker compose up -d

# 按需启动单个服务
docker compose up -d gitlab
docker compose up -d jenkins
docker compose up -d sonarqube
docker compose up -d nexus
docker compose up -d artifactory
docker compose up -d nginx
```

### 各服务访问地址

| 服务 | 地址 | 默认账号 |
|------|------|---------|
| GitLab | http://\<IP\>:8929 | root（初始密码见日志） |
| Jenkins | http://\<IP\>:8081 | admin（初始密码见日志） |
| SonarQube | http://\<IP\>:9000 | admin / admin |
| Nexus | http://\<IP\>:8082 | admin（初始密码见日志） |
| Artifactory | http://\<IP\>:8083 | admin / password |
| Nginx | http://\<IP\>:80 | — |

---

## 离线部署

### 1. 在线机器导出镜像

```bash
docker save gitlab/gitlab-ce:17.0.0-ce.0 | gzip > gitlab-ce-17.0.0.tar.gz
docker save jenkins/jenkins:lts | gzip > jenkins-lts.tar.gz
docker save sonarqube:community | gzip > sonarqube-community.tar.gz
docker save sonatype/nexus3:3.68.0 | gzip > nexus3-3.68.0.tar.gz
docker save releases-docker.jfrog.io/jfrog/artifactory-oss:7.77.5 | gzip > artifactory-oss-7.77.5.tar.gz
docker save nginx:1.27-alpine | gzip > nginx-1.27-alpine.tar.gz
```

### 2. 传输到离线服务器并加载

```bash
scp *.tar.gz docker-compose.yml tools/ user@server:/opt/cicd-lab/

# 离线服务器执行
for f in *.tar.gz; do gunzip -c $f | docker load; done
docker compose up -d
```

---

## 各工具初始化

### GitLab

```bash
# 获取初始 root 密码
docker exec -it gitlab grep 'Password:' /etc/gitlab/initial_root_password
```
- 地址：`http://<IP>:8929`，用户 `root`
- 首次登录后立即修改密码
- 创建 Access Token（`read_repository` 权限）供 Jenkins 使用

### Jenkins

```bash
# 获取初始管理员密码
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```
- 地址：`http://<IP>:8081`
- 必装插件：GitLab Plugin、Pipeline、Docker Pipeline、SonarQube Scanner
- 配置 GitLab 凭据：Manage Jenkins → Credentials → 添加 GitLab API Token

### SonarQube

- 地址：`http://<IP>:9000`，默认 `admin` / `admin`
- 生成 Token：Administration → Security → Users → Tokens
- Jenkins 安装 `SonarQube Scanner` 插件，配置 SonarQube server URL 和 Token

### Nexus

```bash
# 获取初始密码
docker exec nexus cat /nexus-data/admin.password
```
- 地址：`http://<IP>:8082`
- 已预置 `maven-public` 代理仓库，项目 settings.xml 指向即可

### Artifactory

- 地址：`http://<IP>:8083`，默认 `admin` / `password`
- 启动后按 Onboarding Wizard 创建仓库
- 详见 [tools/artifactory/README.md](./tools/artifactory/README.md)

### Nginx

- 在 `tools/nginx/conf.d/` 添加站点 `.conf` 文件
- 重载配置：`docker exec nginx nginx -s reload`

### Harbor（独立部署）

Harbor 组件较重，建议使用官方 installer 单独部署，详见 [tools/harbor/README.md](./tools/harbor/README.md)。

---

## Webhook 联动配置

### Jenkins 创建 Pipeline Job

1. **New Item** → 名称 `ops-monitor` → 选 **Pipeline**
2. Pipeline script from SCM → Git → 填 GitLab 仓库地址
3. Credentials 选择 GitLab Token，Branch 填 `main`

### GitLab 配置 Webhook

GitLab 项目 → **Settings** → **Webhooks**：
- URL：`http://jenkins:8080/project/ops-monitor`
- Trigger：勾选 `Push events`

> 内网环境需在 GitLab Admin Area → Network → Outbound requests 勾选  
> **Allow requests to the local network from webhooks and integrations**

---

## 学习路线

按顺序实践，每个阶段对应 `tools/` 下的一个目录。

| 阶段 | 工具 | 核心任务 |
|------|------|---------|
| 1 | [GitLab](./tools/gitlab/README.md) | 部署仓库、推送代码、MR 协作 |
| 2 | [Jenkins](./tools/jenkins/README.md) | Pipeline Job、Jenkinsfile、Webhook |
| 3 | [SonarQube](./tools/sonarqube/README.md) | 质量扫描、Quality Gate 门禁 |
| 4 | [Nexus](./tools/nexus/README.md) / [Artifactory](./tools/artifactory/README.md) | 代理仓库、缓存依赖、发布制品 |
| 5 | [Docker](./tools/docker/README.md) | Dockerfile、多阶段构建、Compose |
| 6 | [Harbor](./tools/harbor/README.md) | 镜像推送/拉取、漏洞扫描 |
| 7 | [Ansible](./tools/ansible/README.md) | Playbook、批量部署、多环境管理 |
| 8 | [Nginx](./tools/nginx/README.md) | 反向代理、HTTPS、负载均衡 |
| 9 | Jenkinsfile 综合 | 串联所有工具为完整 CI/CD 流水线 |
| 进阶 | [Kubernetes](./tools/kubernetes/README.md) + [ArgoCD](./tools/argocd/README.md) | K3s 集群、GitOps 自动部署 |

### 完整流水线数据流

```
git push → GitLab Webhook → Jenkins 触发
  → SonarQube 扫描（质量门禁）
  → Maven 编译（依赖走 Nexus/Artifactory）
  → Docker 构建镜像 → 推送 Harbor
  → Ansible 部署到目标服务器
  → Nginx 对外服务
```

---

## 常用命令

```bash
# 构建
mvn clean package -DskipTests          # Java
uv pip install -r requirements.txt     # Python
go build -o app.exe                    # Go

# Docker
docker build -t app:1.0 .
docker run -d -p 8080:8080 app:1.0
docker save app:1.0 | gzip > app.tar.gz   # 导出
gunzip -c app.tar.gz | docker load         # 导入

# 服务管理
docker compose up -d <service>
docker compose down
docker compose restart <service>
docker compose logs -f <service>

# 备份
docker exec gitlab gitlab-backup create
docker run --rm -v cicd-lab_jenkins_home:/data -v $(pwd):/backup alpine tar czf /backup/jenkins-backup.tar.gz /data
```

---

## 注意事项

1. **环境区分**：开发环境用 PowerShell，服务器用 Bash
2. **代码规范**：Python 代码必须通过 flake8 检查（max-line-length=120，忽略 E203/W503）
3. **高危操作**：删除文件/目录前必须经过人工审批
4. **镜像隔离**：Docker 打包任务在 `tools/docker/` 内执行，不污染原项目
5. **资源规划**：GitLab 约需 4GB 内存，Jenkins 约需 2GB，建议服务器 8GB+

---

## 参考资源

- [GitLab](https://docs.gitlab.com/) · [Jenkins](https://www.jenkins.io/doc/book/pipeline/syntax/) · [SonarQube](https://docs.sonarsource.com/sonarqube/)
- [Nexus](https://help.sonatype.com/repomanager3) · [Artifactory](https://jfrog.com/help/r/jfrog-artifactory-documentation)
- [Docker](https://docs.docker.com/get-started/) · [Harbor](https://goharbor.io/docs/)
- [Ansible](https://docs.ansible.com/) · [Nginx](https://nginx.org/en/docs/)
- [Kubernetes](https://kubernetes.io/docs/) · [ArgoCD](https://argo-cd.readthedocs.io/)
