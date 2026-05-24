package com.faultlab.service.faults;

import com.faultlab.common.model.FaultParam;
import com.faultlab.util.JvmMetricsUtil;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.stereotype.Component;

/**
 * 堆内存 OOM 使用静态列表持有大对象，便于观察堆增长和 GC 行为。
 */
@Component
public class HeapOomFaultScenario extends AbstractFaultScenario {

    private static final List<byte[]> RETAINED_OBJECTS = new ArrayList<>();
    private final AtomicBoolean running = new AtomicBoolean(false);

    public HeapOomFaultScenario(JvmMetricsUtil jvmMetricsUtil) {
        super(
            "heap-oom",
            "堆内存溢出",
            "初级",
            "向静态列表持续写入 1MB 大小对象，模拟堆内存不断膨胀。",
            "该操作会快速抬升堆使用量，建议在限制堆大小的测试环境运行。",
            true,
            true,
            jvmMetricsUtil
        );
    }

    @Override
    public synchronized void trigger(FaultParam param) {
        running.set(true);
        int blocks = Math.max(10, param.getIntValue("blocks", 32));
        for (int index = 0; index < blocks; index++) {
            RETAINED_OBJECTS.add(new byte[1024 * 1024]);
        }
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("retainedBlocks", RETAINED_OBJECTS.size());
        metrics.put("heapUsedMb", jvmMetricsUtil.getHeapUsedMb());
        markRunning("已分配并持有 " + RETAINED_OBJECTS.size() + " MB 对象", snapshotWith(metrics));
    }

    @Override
    public synchronized void stop() {
        running.set(false);
        RETAINED_OBJECTS.clear();
        System.gc();
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("retainedBlocks", RETAINED_OBJECTS.size());
        metrics.put("heapUsedMb", jvmMetricsUtil.getHeapUsedMb());
        markStopped("已清空静态列表并触发建议性 GC", snapshotWith(metrics));
    }

    @Override
    public String getPrinciple() {
        return """
            ## 原理说明
            - 静态集合被 GC Roots 直接引用，对象始终可达。
            - 对象无法回收时，堆空间会持续增长直到触发 OOM。

            ```mermaid
            graph TD
                A[GC Roots] --> B[Static List]
                B --> C[1MB byte[]]
                B --> D[1MB byte[]]
                B --> E[1MB byte[]]
            ```
            """;
    }

    @Override
    public String getCommands() {
        return """
            ```bash
            jps -l
            jstat -gcutil <pid> 1000
            jmap -histo:live <pid> | head -n 30
            jcmd <pid> GC.heap_info
            arthas dashboard
            ```
            """;
    }
}
