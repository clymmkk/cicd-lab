package com.faultlab.service.faults;

import com.faultlab.common.model.ScenarioStatus;
import com.faultlab.common.model.ScenarioSummary;
import com.faultlab.util.JvmMetricsUtil;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import lombok.Getter;

/**
 * 抽象基类集中维护元数据和状态更新逻辑，让具体故障实现更聚焦。
 */
public abstract class AbstractFaultScenario implements FaultScenario {

    @Getter
    private final String code;
    @Getter
    private final String name;
    @Getter
    private final String level;
    @Getter
    private final String description;
    @Getter
    private final String warning;
    private final boolean dangerous;
    private final boolean stoppable;
    protected final JvmMetricsUtil jvmMetricsUtil;
    private final AtomicReference<ScenarioStatus> statusRef;

    protected AbstractFaultScenario(
        String code,
        String name,
        String level,
        String description,
        String warning,
        boolean dangerous,
        boolean stoppable,
        JvmMetricsUtil jvmMetricsUtil
    ) {
        this.code = code;
        this.name = name;
        this.level = level;
        this.description = description;
        this.warning = warning;
        this.dangerous = dangerous;
        this.stoppable = stoppable;
        this.jvmMetricsUtil = jvmMetricsUtil;
        this.statusRef = new AtomicReference<>(buildStatus("idle", "等待触发", false, new LinkedHashMap<>(), null));
    }

    @Override
    public ScenarioStatus getStatus() {
        return statusRef.get();
    }

    @Override
    public boolean isDangerous() {
        return dangerous;
    }

    @Override
    public boolean isStoppable() {
        return stoppable;
    }

    @Override
    public ScenarioSummary toSummary(boolean enabled) {
        return ScenarioSummary.builder()
            .code(code)
            .name(name)
            .level(level)
            .description(description)
            .warning(warning)
            .dangerous(dangerous)
            .stoppable(stoppable)
            .enabled(enabled)
            .principle(getPrinciple())
            .commands(getCommands())
            .status(getStatus())
            .build();
    }

    protected void markRunning(String message, Map<String, Object> metrics) {
        LocalDateTime startedAt = getStatus().getStartedAt();
        if (startedAt == null) {
            startedAt = LocalDateTime.now();
        }
        statusRef.set(buildStatus("running", message, true, metrics, startedAt));
    }

    protected void markStopped(String message, Map<String, Object> metrics) {
        statusRef.set(buildStatus("recovered", message, false, metrics, getStatus().getStartedAt()));
    }

    protected void markCrashed(String message, Map<String, Object> metrics) {
        statusRef.set(buildStatus("crashed", message, false, metrics, getStatus().getStartedAt()));
    }

    protected Map<String, Object> snapshotWith(Map<String, Object> extraMetrics) {
        Map<String, Object> metrics = new LinkedHashMap<>(jvmMetricsUtil.createSnapshotMap());
        metrics.putAll(extraMetrics);
        return metrics;
    }

    private ScenarioStatus buildStatus(
        String state,
        String message,
        boolean running,
        Map<String, Object> metrics,
        LocalDateTime startedAt
    ) {
        return ScenarioStatus.builder()
            .code(code)
            .name(name)
            .state(state)
            .running(running)
            .stoppable(stoppable)
            .message(message)
            .warning(warning)
            .startedAt(startedAt)
            .updatedAt(LocalDateTime.now())
            .metrics(metrics)
            .build();
    }
}
