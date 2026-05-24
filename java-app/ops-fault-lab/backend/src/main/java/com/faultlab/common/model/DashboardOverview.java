package com.faultlab.common.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * 监控概览承载首页大盘所需的 JVM、线程和连接池指标。
 */
@Data
@Builder
public class DashboardOverview {

    private double heapUsedMb;
    private double heapMaxMb;
    private double nonHeapUsedMb;
    private double cpuLoadPercent;
    private int threadCount;
    private int peakThreadCount;
    private long youngGcCount;
    private long fullGcCount;
    private int activeDbConnections;
    private int idleDbConnections;
    private int totalDbConnections;
    private long redisLatencyMs;
    private String activeScenario;
    @Builder.Default
    private List<String> notices = new ArrayList<>();
}
