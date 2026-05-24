package com.ops.monitor.service;

import com.ops.monitor.entity.Server;
import com.ops.monitor.entity.ServerMetric;
import com.ops.monitor.repository.ServerMetricRepository;
import com.ops.monitor.repository.ServerRepository;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import oshi.util.Util;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SystemMetricsCollector {

    private final ServerMetricRepository metricRepository;
    private final ServerRepository serverRepository;
    private final SystemInfo systemInfo;

    public SystemMetricsCollector(ServerMetricRepository metricRepository,
                                  ServerRepository serverRepository) {
        this.metricRepository = metricRepository;
        this.serverRepository = serverRepository;
        this.systemInfo = new SystemInfo();
    }

    @Scheduled(fixedRate = 60000)
    public void collect() {
        HardwareAbstractionLayer hal = systemInfo.getHardware();
        OperatingSystem os = systemInfo.getOperatingSystem();

        CentralProcessor processor = hal.getProcessor();
        long[] oldTicks = processor.getSystemCpuLoadTicks();
        Util.sleep(1000);
        long[] newTicks = processor.getSystemCpuLoadTicks();
        double cpuUsage = processor.getSystemCpuLoadBetweenTicks(oldTicks) * 100;

        GlobalMemory memory = hal.getMemory();
        long memoryTotal = memory.getTotal() / 1024 / 1024;
        long memoryUsed = (memory.getTotal() - memory.getAvailable()) / 1024 / 1024;

        FileSystem fileSystem = os.getFileSystem();
        long diskTotal = 0;
        long diskUsed = 0;
        for (OSFileStore store : fileSystem.getFileStores()) {
            diskTotal += store.getTotalSpace() / 1024 / 1024;
            diskUsed += (store.getTotalSpace() - store.getUsableSpace()) / 1024 / 1024;
        }

        // 动态获取本地服务器 ID，避免硬编码导致外键约束失败
        Server localServer = serverRepository.findByHost("127.0.0.1").orElse(null);
        if (localServer == null) {
            System.out.println("[MetricsCollector] 本地服务器未注册，跳过本次采集");
            return;
        }

        ServerMetric metric = new ServerMetric();
        metric.setServerId(localServer.getId());
        metric.setCpuUsage(Math.round(cpuUsage * 100.0) / 100.0);
        metric.setMemoryUsed(memoryUsed);
        metric.setMemoryTotal(memoryTotal);
        metric.setDiskUsed(diskUsed);
        metric.setDiskTotal(diskTotal);

        metricRepository.save(metric);
    }
}
