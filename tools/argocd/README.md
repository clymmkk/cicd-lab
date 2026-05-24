# ArgoCD — GitOps 自动部署（进阶）

## 简介
ArgoCD 监听 Git 仓库变化，自动将 Kubernetes 资源同步到集群，实现 GitOps。

## 安装到 K8s 集群
```bash
kubectl create namespace argocd
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml

# 暴露 NodePort
kubectl -n argocd patch svc argocd-server -p '{"spec":{"type":"NodePort"}}'

# 获取初始密码
kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d
```

- 访问：`https://<K8s-NodeIP>:30080`
- 默认账号：`admin`

## 本目录放置内容
- `apps/` — ArgoCD Application 定义
- `sync-policies/` — 同步策略配置

## Application 示例
```yaml
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: ops-monitor
  namespace: argocd
spec:
  project: default
  source:
    repoURL: http://gitlab.local:8929/root/ops-monitor.git
    targetRevision: main
    path: kubernetes/manifests
  destination:
    server: https://kubernetes.default.svc
    namespace: production
  syncPolicy:
    automated:
      prune: true
      selfHeal: true
```

## GitOps 工作流
```
开发者 push 代码 → Jenkins 构建镜像 → 更新 manifests 中的 image tag
    → GitLab 仓库变更 → ArgoCD 检测到 diff → 自动同步到 K8s
```

## 官方文档
https://argo-cd.readthedocs.io/
