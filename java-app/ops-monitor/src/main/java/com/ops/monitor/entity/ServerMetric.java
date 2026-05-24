package com.ops.monitor.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "server_metrics")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServerMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "server_id", nullable = false)
    private Long serverId;

    @Column(name = "cpu_usage")
    private Double cpuUsage;

    @Column(name = "memory_used")
    private Long memoryUsed;

    @Column(name = "memory_total")
    private Long memoryTotal;

    @Column(name = "disk_used")
    private Long diskUsed;

    @Column(name = "disk_total")
    private Long diskTotal;

    @Column(name = "recorded_at")
    private LocalDateTime recordedAt = LocalDateTime.now();
}
