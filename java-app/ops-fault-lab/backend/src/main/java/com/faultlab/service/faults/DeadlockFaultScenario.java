package com.faultlab.service.faults;

import com.faultlab.common.model.FaultParam;
import com.faultlab.util.JvmMetricsUtil;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.stereotype.Component;

/**
 * 死锁场景通过两个线程反向获取锁，稳定制造循环等待。
 */
@Component
public class DeadlockFaultScenario extends AbstractFaultScenario {

    private final Object firstLock = new Object();
    private final Object secondLock = new Object();
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final ThreadMXBean threadMxBean = ManagementFactory.getThreadMXBean();

    public DeadlockFaultScenario(JvmMetricsUtil jvmMetricsUtil) {
        super(
            "deadlock",
            "死锁",
            "中级",
            "两个线程分别持有不同锁后再去申请对方的锁，模拟循环等待。",
            "死锁通常无法自动恢复，建议通过线程 dump 观察并重启服务恢复。",
            true,
            false,
            jvmMetricsUtil
        );
    }

    @Override
    public synchronized void trigger(FaultParam param) {
        if (running.get()) {
            return;
        }
        running.set(true);
        CountDownLatch latch = new CountDownLatch(2);
        Thread first = new Thread(() -> holdLocks(firstLock, secondLock, latch), "fault-deadlock-a");
        Thread second = new Thread(() -> holdLocks(secondLock, firstLock, latch), "fault-deadlock-b");
        first.start();
        second.start();
        try {
            Thread.sleep(300L);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("deadlockedThreads", detectDeadlockedThreads());
        metrics.put("recoverable", false);
        markRunning("死锁线程已启动", snapshotWith(metrics));
    }

    private void holdLocks(Object left, Object right, CountDownLatch latch) {
        synchronized (left) {
            latch.countDown();
            try {
                latch.await();
                Thread.sleep(200L);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            synchronized (right) {
                Thread.onSpinWait();
            }
        }
    }

    private int detectDeadlockedThreads() {
        long[] ids = threadMxBean.findDeadlockedThreads();
        return ids == null ? 0 : ids.length;
    }

    @Override
    public void stop() {
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("deadlockedThreads", detectDeadlockedThreads());
        markStopped("死锁无法自动解除，请重启应用或手动终止线程", snapshotWith(metrics));
    }

    @Override
    public String getPrinciple() {
        return """
            ## 原理说明
            - 线程 A 拿到锁 1 后等待锁 2。
            - 线程 B 拿到锁 2 后等待锁 1。
            - 形成循环等待后，两个线程都无法继续执行。

            ```mermaid
            graph LR
                A[Thread A] -->|holds| L1[Lock 1]
                A -->|waits| L2[Lock 2]
                B[Thread B] -->|holds| L2
                B -->|waits| L1
            ```
            """;
    }

    @Override
    public String getCommands() {
        return """
            ```bash
            jstack <pid>
            jcmd <pid> Thread.print
            arthas thread --state BLOCKED
            ```
            """;
    }
}
