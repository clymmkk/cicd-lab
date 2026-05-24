package com.faultlab.service.faults;

import com.faultlab.common.model.FaultParam;
import com.faultlab.util.JvmMetricsUtil;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.stereotype.Component;

/**
 * 频繁 Full GC 使用大对象和强引用保留策略制造老年代压力。
 */
@Component
public class FullGcFaultScenario extends AbstractFaultScenario {

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final List<byte[]> retainedObjects = new ArrayList<>();
    private volatile Thread workerThread;

    public FullGcFaultScenario(JvmMetricsUtil jvmMetricsUtil) {
        super(
            "full-gc",
            "频繁 Full GC",
            "中级",
            "周期性创建大数组并强引用持有，模拟对象晋升导致的 Full GC 压力。",
            "建议配合较小堆参数观察效果，例如 -Xms256m -Xmx256m -Xmn64m。",
            true,
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
        int retainLimit = Math.max(4, param.getIntValue("retainLimit", 8));
        workerThread = new Thread(() -> allocateLoop(retainLimit), "fault-full-gc");
        workerThread.start();
        markRunning("大对象分配线程已启动", snapshotWith(gcMetrics()));
    }

    private void allocateLoop(int retainLimit) {
        while (running.get()) {
            retainedObjects.add(new byte[10 * 1024 * 1024]);
            if (retainedObjects.size() > retainLimit) {
                retainedObjects.remove(0);
            }
            try {
                TimeUnit.MILLISECONDS.sleep(500L);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
                break;
            }
            markRunning("持续分配大对象中", snapshotWith(gcMetrics()));
        }
    }

    @Override
    public synchronized void stop() {
        running.set(false);
        if (workerThread != null) {
            workerThread.interrupt();
        }
        retainedObjects.clear();
        System.gc();
        markStopped("已停止对象分配并清空强引用", snapshotWith(gcMetrics()));
    }

    private Map<String, Object> gcMetrics() {
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("retainedObjects", retainedObjects.size());
        metrics.put("youngGcCount", jvmMetricsUtil.getYoungGcCount());
        metrics.put("fullGcCount", jvmMetricsUtil.getFullGcCount());
        return metrics;
    }

    @Override
    public String getPrinciple() {
        return """
            ## 原理说明
            - 大对象频繁创建会快速消耗 Eden 和老年代空间。
            - 强引用不释放时，对象晋升和回收压力都会增加。
            - 老年代空间紧张时，Full GC 会更频繁出现。

            ```mermaid
            graph LR
                A[Eden] --> B[Survivor]
                B --> C[Old Gen]
                C --> D[Full GC]
            ```
            """;
    }

    @Override
    public String getCommands() {
        return """
            ```bash
            jstat -gcutil <pid> 1000
            jcmd <pid> GC.heap_info
            jmap -histo <pid> | head -n 30
            arthas dashboard
            ```
            """;
    }
}
