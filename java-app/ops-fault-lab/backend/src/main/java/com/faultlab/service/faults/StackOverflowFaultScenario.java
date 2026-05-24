package com.faultlab.service.faults;

import com.faultlab.common.model.FaultParam;
import com.faultlab.util.JvmMetricsUtil;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.stereotype.Component;

/**
 * 栈溢出风险极高，这里采用独立线程递归，命中后更新状态而不是拖垮主线程。
 */
@Component
public class StackOverflowFaultScenario extends AbstractFaultScenario {

    private final AtomicBoolean running = new AtomicBoolean(false);
    private volatile Thread workerThread;

    public StackOverflowFaultScenario(JvmMetricsUtil jvmMetricsUtil) {
        super(
            "stack-oom",
            "栈内存溢出",
            "初级",
            "通过无出口递归消耗线程栈，模拟 StackOverflowError 场景。",
            "此场景高风险，可能直接导致线程异常退出，极端情况下影响 JVM 稳定性。",
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
        workerThread = new Thread(() -> {
            try {
                recurse(0);
            } catch (StackOverflowError error) {
                Map<String, Object> metrics = new LinkedHashMap<>();
                metrics.put("thread", Thread.currentThread().getName());
                metrics.put("status", "StackOverflowError");
                markCrashed("递归线程已触发 StackOverflowError", snapshotWith(metrics));
            } finally {
                running.set(false);
            }
        }, "fault-stack-overflow");
        workerThread.start();
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("thread", workerThread.getName());
        metrics.put("stoppable", false);
        markRunning("递归线程已启动，请关注进程风险", snapshotWith(metrics));
    }

    private void recurse(int depth) {
        if (!running.get()) {
            return;
        }
        recurse(depth + 1);
    }

    @Override
    public void stop() {
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("stoppable", false);
        markStopped("该场景不支持安全停止，请通过重启应用恢复", snapshotWith(metrics));
    }

    @Override
    public String getPrinciple() {
        return """
            ## 原理说明
            - 每次方法调用都会压入一个新的栈帧。
            - 递归无出口会持续消耗线程栈，最终触发 `StackOverflowError`。

            ```mermaid
            graph TD
                A[main thread] --> B[frame 1]
                B --> C[frame 2]
                C --> D[frame 3]
                D --> E[...]
            ```
            """;
    }

    @Override
    public String getCommands() {
        return """
            ```bash
            jstack <pid>
            jcmd <pid> Thread.print
            arthas thread -n 5
            ```
            """;
    }
}
