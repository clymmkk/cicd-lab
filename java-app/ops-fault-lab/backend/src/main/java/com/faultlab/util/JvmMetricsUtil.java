package com.faultlab.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.management.OperatingSystemMXBean;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import jakarta.annotation.Nullable;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * JVM 指标工具集中读取堆、GC、线程和连接池信息，避免各业务类重复拼装。
 */
@Component
@RequiredArgsConstructor
public class JvmMetricsUtil {

    private final ObjectMapper objectMapper;
    @Nullable
    private final DataSource dataSource;

    public Map<String, Object> createSnapshotMap() {
        MemoryMXBean memoryMxBean = ManagementFactory.getMemoryMXBean();
        ThreadMXBean threadMxBean = ManagementFactory.getThreadMXBean();
        MemoryUsage heapUsage = memoryMxBean.getHeapMemoryUsage();
        MemoryUsage nonHeapUsage = memoryMxBean.getNonHeapMemoryUsage();
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("heapUsedMb", toMb(heapUsage.getUsed()));
        snapshot.put("heapMaxMb", toMb(heapUsage.getMax()));
        snapshot.put("nonHeapUsedMb", toMb(nonHeapUsage.getUsed()));
        snapshot.put("cpuLoadPercent", getCpuLoadPercent());
        snapshot.put("threadCount", threadMxBean.getThreadCount());
        snapshot.put("peakThreadCount", threadMxBean.getPeakThreadCount());
        snapshot.put("youngGcCount", getYoungGcCount());
        snapshot.put("fullGcCount", getFullGcCount());
        snapshot.put("dbPool", getDbPoolSnapshot());
        return snapshot;
    }

    public String createSnapshotJson() {
        try {
            return objectMapper.writeValueAsString(createSnapshotMap());
        } catch (JsonProcessingException exception) {
            return "{\"error\":\"snapshot serialization failed\"}";
        }
    }

    public double getHeapUsedMb() {
        return toMb(ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed());
    }

    public long getYoungGcCount() {
        return getCollectorCount(List.of("Copy", "PS Scavenge", "G1 Young Generation", "ZGC Cycles"));
    }

    public long getFullGcCount() {
        return getCollectorCount(List.of("MarkSweepCompact", "PS MarkSweep", "G1 Old Generation", "ZGC Pauses"));
    }

    public double getCpuLoadPercent() {
        OperatingSystemMXBean operatingSystemMXBean =
            (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        double load = operatingSystemMXBean.getProcessCpuLoad();
        return load < 0 ? 0D : Math.round(load * 10000D) / 100D;
    }

    public Map<String, Integer> getDbPoolSnapshot() {
        Map<String, Integer> snapshot = new LinkedHashMap<>();
        snapshot.put("active", 0);
        snapshot.put("idle", 0);
        snapshot.put("total", 0);
        if (dataSource instanceof HikariDataSource hikariDataSource) {
            HikariPoolMXBean poolMxBean = hikariDataSource.getHikariPoolMXBean();
            if (poolMxBean != null) {
                snapshot.put("active", poolMxBean.getActiveConnections());
                snapshot.put("idle", poolMxBean.getIdleConnections());
                snapshot.put("total", poolMxBean.getTotalConnections());
            }
        }
        return snapshot;
    }

    private long getCollectorCount(List<String> collectorNames) {
        return ManagementFactory.getGarbageCollectorMXBeans().stream()
            .filter(bean -> collectorNames.contains(bean.getName()))
            .mapToLong(GarbageCollectorMXBean::getCollectionCount)
            .filter(count -> count >= 0)
            .sum();
    }

    private double toMb(long bytes) {
        if (bytes < 0) {
            return 0D;
        }
        return Math.round(bytes / 1024D / 1024D * 100D) / 100D;
    }
}
