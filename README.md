# CICD Lab - 持续集成/持续部署实验环境

## 项目简介

本目录用于 Java、Python、Go 等多语言的 CI/CD 实验，提供标准化的构建、测试、部署流程参考。

## 环境准备

### 基础工具

| 工具 | 版本要求 | 说明 |
|------|----------|------|
| Git | >= 2.30 | 版本控制 |
| Docker | >= 20.10 | 容器化构建与部署 |
| Docker Compose | >= 2.0 | 多容器编排 |

### 语言环境

#### Java
- JDK 11 或 JDK 17
- Maven >= 3.8 或 Gradle >= 7.0

#### Python
- Python >= 3.9
- uv（包管理器，优先使用）
- pip（备用包管理器）

#### Go
- Go >= 1.21

### 安装命令（Windows 11 / PowerShell）

```powershell
# 使用 winget 安装基础工具
winget install Git.Git
winget install Docker.DockerDesktop

# 使用 uv 安装 Python（推荐）
irm https://astral.sh/uv/install.ps1 | iex

# 或使用 pip
python -m pip install --upgrade pip
```

## 目录结构

```
cicd-lab/
├── java-app/          # Java 项目实验目录
├── python-app/        # Python 项目实验目录
├── go-app/            # Go 项目实验目录
├── .github/           # GitHub Actions CI/CD 配置（可选）
├── Jenkinsfile        # Jenkins 流水线配置（可选）
├── docker-compose.yml # 多服务编排（可选）
├── README.md          # 项目说明文档
└── DEPLOY.md          # 部署文档
```

## 快速启动

### Python 项目

```powershell
# 创建项目目录
mkdir python-app
cd python-app

# 初始化虚拟环境
uv venv
.\.venv\Scripts\Activate.ps1

# 安装依赖（使用清华源）
uv pip install -i https://pypi.tuna.tsinghua.edu.cn/simple <package_name>

# 代码检查
flake8 --max-line-length=120 --ignore=E203,W503 .

# 运行项目
python main.py
```

### Java 项目

```powershell
# 使用 Maven 创建项目
mvn archetype:generate `
  -DgroupId=com.example `
  -DartifactId=java-app `
  -DarchetypeArtifactId=maven-archetype-quickstart `
  -DinteractiveMode=false

# 构建项目
mvn clean package

# 运行项目
java -jar target/java-app-1.0-SNAPSHOT.jar
```

### Go 项目

```powershell
# 初始化 Go 模块
mkdir go-app
cd go-app
go mod init github.com/example/go-app

# 创建主文件并运行
go run main.go

# 构建可执行文件
go build -o go-app.exe
```

## CI/CD 实验内容

### 1. 基础构建流水线
- 代码拉取
- 依赖安装
- 代码编译/打包
- 单元测试

### 2. 代码质量检查
- Java: SonarQube / Checkstyle
- Python: flake8 / pylint / mypy
- Go: golangci-lint

### 3. 容器化部署
- 编写 Dockerfile
- 构建 Docker 镜像
- 推送至镜像仓库
- Docker Compose 编排

### 4. 自动化部署
- Jenkins Pipeline
- GitHub Actions
- GitLab CI

## 常用命令速查

### Docker 镜像打包与分发

```powershell
# 打包镜像
docker save <image_name>:<tag> | gzip > <image_name>.tar.gz

# 导入镜像
gunzip -c <image_name>.tar.gz | docker load

# 构建镜像
docker build -t <image_name>:<tag> .

# 运行容器
docker run -d -p 8080:8080 <image_name>:<tag>
```

### Python 包管理

```powershell
# 使用 uv 安装（推荐）
uv pip install -i https://pypi.tuna.tsinghua.edu.cn/simple <package>

# 使用 pip 安装（备用）
pip install -i https://pypi.tuna.tsinghua.edu.cn/simple <package>

# 导出依赖
pip freeze > requirements.txt
```

## 注意事项

1. **环境区分**：开发环境使用 PowerShell 语法，测试/生产环境使用 Linux Shell 语法
2. **代码规范**：Python 代码必须通过 flake8 检查（max-line-length=120，忽略 E203/W503）
3. **高危操作**：删除文件/目录前必须经过人工审批
4. **镜像隔离**：Docker 相关打包任务在专用目录内执行，不污染原项目

## 参考资源

- [Docker 官方文档](https://docs.docker.com/)
- [GitHub Actions 文档](https://docs.github.com/en/actions)
- [Jenkins 官方文档](https://www.jenkins.io/doc/)
- [uv 包管理器](https://docs.astral.sh/uv/)
