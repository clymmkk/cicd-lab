# Harbor — 企业级镜像仓库

## 简介
Harbor 提供镜像签名、漏洞扫描、RBAC 权限管控，适合企业级镜像管理。

## 部署方式
Harbor 组件较多（Core、Database、Redis、Registry、Trivy 等），建议使用官方 installer 独立部署：

```bash
# 下载安装包
wget https://github.com/goharbor/harbor/releases/download/v2.11.0/harbor-offline-installer-v2.11.0.tgz
tar xzf harbor-offline-installer-v2.11.0.tgz
cd harbor

# 复制并修改配置
cp harbor.yml.tmpl harbor.yml
# 编辑 harbor.yml，修改 hostname、端口、密码等

# 安装并启动
sudo ./install.sh --with-trivy
```

- 访问：`https://<IP>:8443`
- 默认账号：`admin` / `Harbor12345`

## 本目录放置内容
- `harbor.yml` — Harbor 配置文件
- `certs/` — HTTPS 证书

## 离线部署
```bash
# 在线机器导出镜像包
docker save $(docker images --format '{{.Repository}}:{{.Tag}}' | grep goharbor) | gzip > harbor-images.tar.gz

# 离线服务器加载
gunzip -c harbor-images.tar.gz | docker load
# 再执行 ./install.sh
```

## 官方文档
https://goharbor.io/docs/
