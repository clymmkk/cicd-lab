package com.ops.monitor.service;

import com.ops.monitor.dto.ServerMetricDto;
import com.ops.monitor.entity.ServerMetric;
import com.ops.monitor.repository.ServerMetricRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServerMetricService {

    private final ServerMetricRepository metricRepository;

    public ServerMetricService(ServerMetricRepository metricRepository) {
        this.metricRepository = metricRepository;
    }

    public List<ServerMetricDto> getMetricsByServerId(Long serverId) {
        return metricRepository.findByServerIdOrderByRecordedAtDesc(serverId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public ServerMetricDto getLatestMetric(Long serverId) {
        ServerMetric metric = metricRepository.findFirstByServerIdOrderByRecordedAtDesc(serverId);
        return metric != null ? toDto(metric) : null;
    }

    public List<ServerMetricDto> getRecentMetrics(Long serverId) {
        return metricRepository.findTop10ByServerIdOrderByRecordedAtDesc(serverId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public ServerMetricDto saveMetric(ServerMetric metric) {
        return toDto(metricRepository.save(metric));
    }

    private ServerMetricDto toDto(ServerMetric metric) {
        return new ServerMetricDto(
                metric.getId(),
                metric.getServerId(),
                metric.getCpuUsage(),
                metric.getMemoryUsed(),
                metric.getMemoryTotal(),
                metric.getDiskUsed(),
                metric.getDiskTotal(),
                metric.getRecordedAt()
        );
    }
}
