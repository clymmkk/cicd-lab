package com.ops.monitor.controller;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import oshi.util.Util;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/metrics")
public class CurrentMetricController {

    @GetMapping("/current")
    public Map<String, Object> getCurrentMetrics() {
        SystemInfo systemInfo = new SystemInfo();
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

        Map<String, Object> result = new HashMap<>();
        result.put("cpuUsage", Math.round(cpuUsage * 100.0) / 100.0);
        result.put("memoryUsed", memoryUsed);
        result.put("memoryTotal", memoryTotal);
        result.put("diskUsed", diskUsed);
        result.put("diskTotal", diskTotal);
        result.put("os", os.toString());

        return result;
    }
}
