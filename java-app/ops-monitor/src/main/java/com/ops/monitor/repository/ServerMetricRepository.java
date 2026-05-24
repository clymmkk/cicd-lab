package com.ops.monitor.repository;

import com.ops.monitor.entity.ServerMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServerMetricRepository extends JpaRepository<ServerMetric, Long> {

    List<ServerMetric> findByServerIdOrderByRecordedAtDesc(Long serverId);

    List<ServerMetric> findTop10ByServerIdOrderByRecordedAtDesc(Long serverId);

    ServerMetric findFirstByServerIdOrderByRecordedAtDesc(Long serverId);
}
