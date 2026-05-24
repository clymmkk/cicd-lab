package com.faultlab.service;

import com.faultlab.common.exception.BizException;
import com.faultlab.common.model.DashboardOverview;
import com.faultlab.common.model.FaultParam;
import com.faultlab.common.model.ScenarioSummary;
import com.faultlab.entity.FaultLogEntity;
import com.faultlab.entity.FaultScenarioEntity;
import com.faultlab.entity.OperationLogEntity;
import com.faultlab.service.faults.FaultScenario;
import com.faultlab.util.JvmMetricsUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 门面服务把控制器需要的查询和场景编排收敛到一个入口。
 */
@Service
@RequiredArgsConstructor
public class FaultLabFacade {

    private final List<FaultScenario> faultScenarios;
    private final FaultScenarioConfigService faultScenarioConfigService;
    private final ScenarioRuntimeCoordinator runtimeCoordinator;
    private final FaultLogService faultLogService;
    private final OperationLogService operationLogService;
    private final JvmMetricsUtil jvmMetricsUtil;

    public List<ScenarioSummary> listScenarios() {
        Map<String, FaultScenarioEntity> configs = faultScenarioConfigService.listAll().stream()
            .collect(Collectors.toMap(FaultScenarioEntity::getScenarioCode, entity -> entity));
        return faultScenarios.stream()
            .sorted(Comparator.comparing(FaultScenario::getCode))
            .map(scenario -> {
                FaultScenarioEntity entity = configs.get(scenario.getCode());
                boolean enabled = entity == null || !Boolean.FALSE.equals(entity.getIsEnabled());
                return scenario.toSummary(enabled);
            })
            .toList();
    }

    public ScenarioSummary getScenario(String code) {
        FaultScenario scenario = getScenarioBean(code);
        FaultScenarioEntity entity = faultScenarioConfigService.findByCode(code).orElse(null);
        boolean enabled = entity == null || !Boolean.FALSE.equals(entity.getIsEnabled());
        return scenario.toSummary(enabled);
    }

    public ScenarioSummary triggerScenario(String code, FaultParam param, HttpServletRequest request) {
        FaultScenario scenario = getScenarioBean(code);
        FaultScenarioEntity config = faultScenarioConfigService.requireEnabled(code);
        FaultParam finalParam = faultScenarioConfigService.applyDefaults(config, param);
        runtimeCoordinator.beginScenario(code, scenario.getName(), finalParam.getOperator(), getClientIp(request));
        try {
            scenario.trigger(finalParam);
            return scenario.toSummary(true);
        } catch (Exception exception) {
            runtimeCoordinator.reportCrash(code, scenario.getName(), finalParam.getOperator(), getClientIp(request));
            throw exception;
        }
    }

    public ScenarioSummary stopScenario(String code, FaultParam param, HttpServletRequest request) {
        FaultScenario scenario = getScenarioBean(code);
        if (!scenario.isStoppable()) {
            throw new BizException("SCENARIO_NOT_STOPPABLE", "当前场景无法安全停止，请通过重启应用恢复");
        }
        scenario.stop();
        runtimeCoordinator.finishScenario(code, scenario.getName(), param == null ? "ops_user" : param.getOperator(), getClientIp(request));
        return scenario.toSummary(true);
    }

    public DashboardOverview getDashboard() {
        Map<String, Object> snapshot = jvmMetricsUtil.createSnapshotMap();
        Map<String, Integer> dbPool = jvmMetricsUtil.getDbPoolSnapshot();
        String activeScenario = runtimeCoordinator.getActiveScenarioCode().get();
        DashboardOverview overview = DashboardOverview.builder()
            .heapUsedMb((Double) snapshot.get("heapUsedMb"))
            .heapMaxMb((Double) snapshot.get("heapMaxMb"))
            .nonHeapUsedMb((Double) snapshot.get("nonHeapUsedMb"))
            .cpuLoadPercent(jvmMetricsUtil.getCpuLoadPercent())
            .threadCount((Integer) snapshot.get("threadCount"))
            .peakThreadCount((Integer) snapshot.get("peakThreadCount"))
            .youngGcCount(jvmMetricsUtil.getYoungGcCount())
            .fullGcCount(jvmMetricsUtil.getFullGcCount())
            .activeDbConnections(dbPool.getOrDefault("active", 0))
            .idleDbConnections(dbPool.getOrDefault("idle", 0))
            .totalDbConnections(dbPool.getOrDefault("total", 0))
            .redisLatencyMs(extractRedisLatency())
            .activeScenario(activeScenario)
            .build();
        if (activeScenario != null) {
            overview.getNotices().add("当前运行故障场景: " + activeScenario);
        } else {
            overview.getNotices().add("当前无活动故障，可安全触发新场景。");
        }
        return overview;
    }

    public List<FaultLogEntity> listFaultLogs(String type, LocalDateTime from, LocalDateTime to) {
        return faultLogService.listFaultLogs(type, from, to);
    }

    public List<OperationLogEntity> listOperationLogs() {
        return operationLogService.listRecent(50);
    }

    private long extractRedisLatency() {
        Optional<FaultScenario> scenario = faultScenarios.stream()
            .filter(item -> "redis-latency".equals(item.getCode()))
            .findFirst();
        if (scenario.isEmpty()) {
            return 0L;
        }
        Object value = scenario.get().getStatus().getMetrics().get("redisLatencyMs");
        if (value instanceof Number number) {
            return number.longValue();
        }
        return 0L;
    }

    private FaultScenario getScenarioBean(String code) {
        return faultScenarios.stream()
            .filter(item -> item.getCode().equals(code))
            .findFirst()
            .orElseThrow(() -> new BizException("SCENARIO_NOT_FOUND", "未找到故障场景: " + code));
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
