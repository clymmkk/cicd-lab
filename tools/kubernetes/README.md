# Kubernetes — 容器编排（进阶）

## 简介
Kubernetes（K8s）自动化容器部署、扩缩容和管理。实验环境推荐 K3s 轻量发行版。

## K3s 快速安装
```bash
curl -sfL https://rancher-mirror.rancher.cn/k3s/k3s-install.sh | INSTALL_K3S_MIRROR=cn sh -s - --docker
```

## 本目录放置内容
- `manifests/` — Deployment、Service、Ingress 等 YAML
- `helm/` — Helm Chart 配置
- `kubeconfig` — 集群访问配置

## 部署示例
```yaml
# manifests/ops-monitor-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ops-monitor
spec:
  replicas: 2
  selector:
    matchLabels:
      app: ops-monitor
  template:
    metadata:
      labels:
        app: ops-monitor
    spec:
      containers:
        - name: ops-monitor
          image: harbor.local/ops-monitor:latest
          ports:
            - containerPort: 8080
---
apiVersion: v1
kind: Service
metadata:
  name: ops-monitor
spec:
  selector:
    app: ops-monitor
  ports:
    - port: 80
      targetPort: 8080
  type: ClusterIP
```

## 官方文档
https://kubernetes.io/docs/
