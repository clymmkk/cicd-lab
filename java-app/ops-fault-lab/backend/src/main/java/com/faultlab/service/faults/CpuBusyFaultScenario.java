package com.faultlab.service.faults;

import com.faultlab.common.model.FaultParam;
import com.faultlab.util.JvmMetricsUtil;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.stereotype.Component;

/**
 * CPU 飙高场景通过紧密循环制造单核高占用，便于排查热点线程。
 */
@Component
public class CpuBusyFaultScenario extends AbstractFaultScenario {

    private final AtomicBoolean running = new AtomicBoolean(false);
    private volatile Thread workerThread;

    public CpuBusyFaultScenario(JvmMetricsUtil jvmMetricsUtil) {
        super(
            "cpu-busy",
            "CPU 飙高",
            "初级",
            "通过空转死循环持续消耗 CPU 时间片。",
            "空循环会持续占用一个 CPU 核心，建议观察 top 和 jstack 结果。",
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
        workerThread = new Thread(() -> {
            while (running.get()) {
                Thread.onSpinWait();
            }
        }, "fault-cpu-busy");
        workerThread.start();
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("thread", workerThread.getName());
        metrics.put("cpuLoadPercent", jvmMetricsUtil.getCpuLoadPercent());
        markRunning("忙循环线程已启动", snapshotWith(metrics));
    }

    @Override
    public synchronized void stop() {
        running.set(false);
        if (workerThread != null) {
            workerThread.interrupt();
        }
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("thread", workerThread == null ? null : workerThread.getName());
        metrics.put("cpuLoadPercent", jvmMetricsUtil.getCpuLoadPercent());
        markStopped("忙循环线程已停止", snapshotWith(metrics));
    }

    @Override
    public String getPrinciple() {
        return """
            ## 原理说明
            - `while(true)` 空循环不发生阻塞，线程会反复占用时间片。
            - 操作系统调度器会持续让该线程进入运行态，因此 CPU 使用率升高。

            ```mermaid
            graph LR
                A[Runnable Thread] --> B[CPU Time Slice]
                B --> A
            ```
            """;
    }

    @Override
    public String getCommands() {
        return """
            ```bash
            top -Hp <pid>
            printf "%x\n" <tid>
            jstack <pid> | grep -A 20 <nid>
            arthas thread -n 5
            ```
            """;
    }
}
