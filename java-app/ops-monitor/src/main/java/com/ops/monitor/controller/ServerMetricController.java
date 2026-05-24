package com.ops.monitor.controller;

import com.ops.monitor.dto.ServerMetricDto;
import com.ops.monitor.service.ServerMetricService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/metrics")
public class ServerMetricController {

    private final ServerMetricService metricService;

    public ServerMetricController(ServerMetricService metricService) {
        this.metricService = metricService;
    }

    @GetMapping("/{serverId}")
    public ResponseEntity<List<ServerMetricDto>> getMetrics(@PathVariable Long serverId) {
        return ResponseEntity.ok(metricService.getMetricsByServerId(serverId));
    }

    @GetMapping("/{serverId}/latest")
    public ResponseEntity<ServerMetricDto> getLatestMetric(@PathVariable Long serverId) {
        return ResponseEntity.ok(metricService.getLatestMetric(serverId));
    }

    @GetMapping("/{serverId}/recent")
    public ResponseEntity<List<ServerMetricDto>> getRecentMetrics(@PathVariable Long serverId) {
        return ResponseEntity.ok(metricService.getRecentMetrics(serverId));
    }
}
