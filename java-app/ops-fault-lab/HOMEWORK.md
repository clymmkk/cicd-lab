# HOMEWORK

## 课后作业

1. 扩展一个 `Metaspace OOM` 场景，尝试使用 CGLIB 或 ByteBuddy 动态生成类。
2. 修改线程池拒绝策略为 `AbortPolicy`、`DiscardPolicy`、`CallerRunsPolicy`，对比接口响应差异。
3. 使用 JMeter 编写压测脚本，在 `cpu-busy` 和 `db-pool-exhaustion` 场景下观察系统表现。
4. 给后端增加登录鉴权，思考故障触发接口在生产环境的安全边界。
5. 在 CI/CD 流水线中加入 `mvn test`、`npm run build`、Docker 镜像构建和制品归档。
