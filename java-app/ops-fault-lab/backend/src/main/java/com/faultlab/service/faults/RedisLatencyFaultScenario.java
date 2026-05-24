package com.faultlab.service.faults;

import com.faultlab.common.model.FaultParam;
import com.faultlab.util.JvmMetricsUtil;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

/**
 * Redis 慢查询场景优先使用 DEBUG SLEEP，受限时回退到 Lua 循环制造阻塞。
 */
@Slf4j
@Component
public class RedisLatencyFaultScenario extends AbstractFaultScenario {

    private final RedisConnectionFactory redisConnectionFactory;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private volatile long lastLatencyMs;

    public RedisLatencyFaultScenario(JvmMetricsUtil jvmMetricsUtil, RedisConnectionFactory redisConnectionFactory) {
        super(
            "redis-latency",
            "Redis 连接超时 / 慢查询",
            "高级",
            "通过 Redis 慢命令制造阻塞，模拟缓存响应时间陡增。",
            "受 Redis 配置影响，DEBUG SLEEP 可能被禁用，平台会自动回退到 Lua 计算方案。",
            false,
            true,
            jvmMetricsUtil
        );
        this.redisConnectionFactory = redisConnectionFactory;
    }

    @Override
    public synchronized void trigger(FaultParam param) {
        running.set(true);
        int sleepSeconds = Math.max(3, param.getIntValue("sleepSeconds", 5));
        Instant start = Instant.now();
        try (RedisConnection connection = redisConnectionFactory.getConnection()) {
            try {
                connection.execute("DEBUG", "SLEEP".getBytes(), String.valueOf(sleepSeconds).getBytes());
            } catch (Exception debugException) {
                log.warn("DEBUG SLEEP unavailable, fallback to EVAL", debugException);
                String script = "local sum=0; for i=1,2500000 do sum=sum+i end; return sum";
                connection.scriptingCommands().eval(script.getBytes(), org.springframework.data.redis.connection.ReturnType.INTEGER, 0);
            }
        }
        lastLatencyMs = Duration.between(start, Instant.now()).toMillis();
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("redisLatencyMs", lastLatencyMs);
        markRunning("Redis 慢命令执行完成，耗时 " + lastLatencyMs + " ms", snapshotWith(metrics));
    }

    @Override
    public synchronized void stop() {
        running.set(false);
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("redisLatencyMs", lastLatencyMs);
        markStopped("已停止 Redis 故障注入", snapshotWith(metrics));
    }

    @Override
    public String getPrinciple() {
        return """
            ## 原理说明
            - Redis 单线程处理命令。
            - 慢命令执行期间，同一实例上的其他请求会被阻塞。
            - 网络 RTT 与命令执行时间叠加后，客户端表现为超时或延迟升高。

            ```mermaid
            graph LR
                A[Client Requests] --> B[Redis Event Loop]
                B --> C[Slow Command]
                C --> D[Other Requests Waiting]
            ```
            """;
    }

    @Override
    public String getCommands() {
        return """
            ```bash
            redis-cli slowlog get 10
            redis-cli info commandstats
            redis-cli latency latest
            arthas watch com.faultlab..* '*' -x 2
            ```
            """;
    }
}
