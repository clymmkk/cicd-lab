package com.ops.monitor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServerMetricDto {
    private Long id;
    private Long serverId;
    private Double cpuUsage;
    private Long memoryUsed;
    private Long memoryTotal;
    private Long diskUsed;
    private Long diskTotal;
    private LocalDateTime recordedAt;
}
