package com.faultlab.service;

import com.faultlab.common.exception.BizException;
import com.faultlab.util.JvmMetricsUtil;
import java.util.concurrent.atomic.AtomicReference;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 运行协调器负责“同一时刻只允许一个故障运行”的防护逻辑。
 */
@Component
@RequiredArgsConstructor
public class ScenarioRuntimeCoordinator {

    private final FaultLogService faultLogService;
    private final OperationLogService operationLogService;
    private final JvmMetricsUtil jvmMetricsUtil;

    @Getter
    private final AtomicReference<String> activeScenarioCode = new AtomicReference<>();
    private final AtomicReference<Long> activeFaultLogId = new AtomicReference<>();

    public void beginScenario(String scenarioCode, String scenarioName, String operator, String ip) {
        String current = activeScenarioCode.get();
        if (current != null && !current.equals(scenarioCode)) {
            throw new BizException("FAULT_ALREADY_RUNNING", "当前已有故障运行中: " + current);
        }
        if (!activeScenarioCode.compareAndSet(null, scenarioCode)) {
            throw new BizException("FAULT_ALREADY_RUNNING", "当前已有故障运行中");
        }
        Long logId = faultLogService.createRunningLog(scenarioCode, operator, jvmMetricsUtil.createSnapshotJson());
        activeFaultLogId.set(logId);
        operationLogService.logAction(operator, "触发故障场景: " + scenarioName, ip);
    }

    public void finishScenario(String scenarioCode, String scenarioName, String operator, String ip) {
        if (scenarioCode.equals(activeScenarioCode.get())) {
            faultLogService.markRecovered(activeFaultLogId.get(), jvmMetricsUtil.createSnapshotJson());
            operationLogService.logAction(operator, "停止故障场景: " + scenarioName, ip);
            activeScenarioCode.set(null);
            activeFaultLogId.set(null);
        }
    }

    public void reportCrash(String scenarioCode, String scenarioName, String operator, String ip) {
        if (scenarioCode.equals(activeScenarioCode.get())) {
            faultLogService.markCrashed(activeFaultLogId.get(), jvmMetricsUtil.createSnapshotJson());
            operationLogService.logAction(operator, "场景异常结束: " + scenarioName, ip);
            activeScenarioCode.set(null);
            activeFaultLogId.set(null);
        }
    }
}
