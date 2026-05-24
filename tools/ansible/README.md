# Ansible — 自动化部署与配置管理

## 简介
Ansible 通过 SSH 无代理管理服务器，用 Playbook 描述自动化任务。

## 安装（控制节点）
```bash
pip install ansible
```

## 本目录放置内容
- `inventory/` — 主机清单（dev/staging/prod）
- `playbooks/` — 部署 Playbook
- `roles/` — 可复用角色
- `ansible.cfg` — Ansible 配置

## 示例：部署 Java 应用
```yaml
# playbooks/deploy-ops-monitor.yml
---
- hosts: app_servers
  become: yes
  tasks:
    - name: 安装 JDK 17
      apt:
        name: openjdk-17-jre
        state: present

    - name: 停止旧服务
      systemd:
        name: ops-monitor
        state: stopped
      ignore_errors: yes

    - name: 部署 jar 包
      copy:
        src: target/ops-monitor.jar
        dest: /opt/ops-monitor/app.jar

    - name: 启动服务
      systemd:
        name: ops-monitor
        state: started
        enabled: yes
```

## 执行
```bash
ansible-playbook -i inventory/dev.ini playbooks/deploy-ops-monitor.yml
```

## 官方文档
https://docs.ansible.com/
