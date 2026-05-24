package com.faultlab.controller;

import com.faultlab.common.api.Result;
import com.faultlab.common.model.DashboardOverview;
import com.faultlab.common.model.FaultParam;
import com.faultlab.common.model.ScenarioSummary;
import com.faultlab.entity.FaultLogEntity;
import com.faultlab.entity.OperationLogEntity;
import com.faultlab.service.FaultLabFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 故障平台控制器对外提供场景控制、监控大盘和历史记录接口。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Fault Lab", description = "故障演练平台接口")
public class FaultScenarioController {

    private final FaultLabFacade faultLabFacade;

    @GetMapping("/scenarios")
    @Operation(summary = "查询所有故障场景")
    public Result<List<ScenarioSummary>> listScenarios() {
        return Result.success(faultLabFacade.listScenarios());
    }

    @GetMapping("/scenarios/{code}")
    @Operation(summary = "查询单个故障场景详情")
    public Result<ScenarioSummary> getScenario(@PathVariable String code) {
        return Result.success(faultLabFacade.getScenario(code));
    }

    @PostMapping("/scenarios/{code}/trigger")
    @Operation(summary = "触发故障场景")
    public Result<ScenarioSummary> triggerScenario(
        @PathVariable String code,
        @Valid @RequestBody(required = false) FaultParam faultParam,
        HttpServletRequest request
    ) {
        return Result.success("场景已触发", faultLabFacade.triggerScenario(code, faultParam, request));
    }

    @PostMapping("/scenarios/{code}/stop")
    @Operation(summary = "停止故障场景")
    public Result<ScenarioSummary> stopScenario(
        @PathVariable String code,
        @RequestBody(required = false) FaultParam faultParam,
        HttpServletRequest request
    ) {
        return Result.success("场景已停止", faultLabFacade.stopScenario(code, faultParam, request));
    }

    @GetMapping("/dashboard")
    @Operation(summary = "查询监控大盘概览")
    public Result<DashboardOverview> getDashboard() {
        return Result.success(faultLabFacade.getDashboard());
    }

    @GetMapping("/fault-logs")
    @Operation(summary = "查询故障触发记录")
    public Result<List<FaultLogEntity>> listFaultLogs(
        @RequestParam(required = false) String faultType,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime from,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime to
    ) {
        return Result.success(faultLabFacade.listFaultLogs(faultType, from, to));
    }

    @GetMapping("/operation-logs")
    @Operation(summary = "查询操作审计日志")
    public Result<List<OperationLogEntity>> listOperationLogs() {
        return Result.success(faultLabFacade.listOperationLogs());
    }
}
