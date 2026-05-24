# Nginx — 反向代理与负载均衡

## 简介
Nginx 作为统一入口，提供反向代理、HTTPS 卸载、负载均衡和动静分离。

## 快速启动
```bash
docker compose up -d nginx
```
- HTTP：`80`
- HTTPS：`443`

## 本目录放置内容
- `nginx.conf` — 主配置文件
- `conf.d/` — 站点配置（每个服务一个 .conf）
- `ssl/` — HTTPS 证书文件

## 反向代理示例
```nginx
# conf.d/ops-monitor.conf
server {
    listen 80;
    server_name ops-monitor.local;

    location / {
        proxy_pass http://192.168.10.31:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## 负载均衡示例
```nginx
upstream app_backend {
    server 192.168.10.31:8080 weight=3;
    server 192.168.10.32:8080 weight=1;
}
```

## 官方文档
https://nginx.org/en/docs/
