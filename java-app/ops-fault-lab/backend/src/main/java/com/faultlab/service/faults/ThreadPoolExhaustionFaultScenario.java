package com.faultlab.service.faults;

import com.faultlab.common.model.FaultParam;
import com.faultlab.util.JvmMetricsUtil;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.stereotype.Component;

/**
 * 线程池耗尽通过小线程池与小队列堆积慢任务，方便观察活跃线程和队列长度。
 */
@Component
public class ThreadPoolExhaustionFaultScenario extends AbstractFaultScenario {

    private final AtomicBoolean running = new AtomicBoolean(false);
    private volatile ThreadPoolExecutor executor;

    public ThreadPoolExhaustionFaultScenario(JvmMetricsUtil jvmMetricsUtil) {
        super(
            "thread-pool-exhaustion",
            "线程池耗尽",
            "中级",
            "固定大小线程池持续提交慢任务，模拟工作线程和队列被打满。",
            "被拒绝的任务会走 CallerRunsPolicy，接口触发线程也可能变慢。",
            false,
            true,
            jvmMetricsUtil
        );
    }

    @Override
    public synchronized void trigger(FaultParam param) {
        if (running.get()) {
            return;
        }
        running.set(true);
        int taskCount = Math.max(12, param.getIntValue("tasks", 20));
        int sleepSeconds = Math.max(10, param.getIntValue("sleepSeconds", 30));
        executor = new ThreadPoolExecutor(
            3,
            3,
            0L,
            TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(10),
            new ThreadPoolExecutor.CallerRunsPolicy()
        );
        for (int index = 0; index < taskCount; index++) {
            executor.submit(() -> {
                try {
                    TimeUnit.SECONDS.sleep(sleepSeconds);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        markRunning(buildMessage(), snapshotWith(poolMetrics()));
    }

    @Override
    public synchronized void stop() {
        running.set(false);
        if (executor != null) {
            executor.shutdownNow();
        }
        markStopped("线程池已 shutdownNow", snapshotWith(poolMetrics()));
    }

    private String buildMessage() {
        return "线程池已压入慢任务，活跃线程 " + poolMetrics().get("activeCount")
            + "，队列剩余 " + poolMetrics().get("queueRemainingCapacity");
    }

    private Map<String, Object> poolMetrics() {
        Map<String, Object> metrics = new LinkedHashMap<>();
        if (executor == null) {
            metrics.put("activeCount", 0);
            metrics.put("poolSize", 0);
            metrics.put("queueRemainingCapacity", 10);
            metrics.put("queuedTasks", 0);
            return metrics;
        }
        metrics.put("activeCount", executor.getActiveCount());
        metrics.put("poolSize", executor.getPoolSize());
        metrics.put("queueRemainingCapacity", executor.getQueue().remainingCapacity());
        metrics.put("queuedTasks", executor.getQueue().size());
        return metrics;
    }

    @Override
    public String getPrinciple() {
        return """
            ## 原理说明
            - 固定线程池只有有限工作线程。
            - 当慢任务持续堆积时，队列会被填满。
            - 队列和线程都耗尽后，拒绝策略开始生效。

            ```mermaid
            graph LR
                A[Incoming Tasks] --> B[Pool Size 3]
                A --> C[Queue Size 10]
                C --> D[CallerRunsPolicy]
            ```
            """;
    }

    @Override
    public String getCommands() {
        return """
            ```bash
            jstack <pid>
            jcmd <pid> Thread.print
            arthas thread
            arthas dashboard
            ```
            """;
    }
}
