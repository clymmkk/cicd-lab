package com.faultlab.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.faultlab.entity.FaultLogEntity;
import com.faultlab.mapper.FaultLogMapper;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Service;

/**
 * 故障记录服务对外提供新增、恢复、崩溃和筛选查询。
 */
@Service
@RequiredArgsConstructor
public class FaultLogService {

    private final FaultLogMapper faultLogMapper;

    public Long createRunningLog(String faultType, String operator, String metricsSnapshot) {
        FaultLogEntity entity = new FaultLogEntity();
        entity.setFaultType(faultType);
        entity.setOperator(operator);
        entity.setTriggerTime(LocalDateTime.now());
        entity.setJvmMetrics(metricsSnapshot);
        entity.setStatus("running");
        faultLogMapper.insert(entity);
        return entity.getId();
    }

    public void markRecovered(Long id, String metricsSnapshot) {
        updateStatus(id, "recovered", metricsSnapshot);
    }

    public void markCrashed(Long id, String metricsSnapshot) {
        updateStatus(id, "crashed", metricsSnapshot);
    }

    public List<FaultLogEntity> listFaultLogs(
        String faultType,
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime from,
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime to
    ) {
        LambdaQueryWrapper<FaultLogEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(faultType != null && !faultType.isBlank(), FaultLogEntity::getFaultType, faultType)
            .ge(from != null, FaultLogEntity::getTriggerTime, from)
            .le(to != null, FaultLogEntity::getTriggerTime, to)
            .orderByDesc(FaultLogEntity::getTriggerTime);
        return faultLogMapper.selectList(wrapper);
    }

    private void updateStatus(Long id, String status, String metricsSnapshot) {
        if (id == null) {
            return;
        }
        FaultLogEntity entity = faultLogMapper.selectById(id);
        if (entity == null) {
            return;
        }
        entity.setStatus(status);
        entity.setRecoveryTime(LocalDateTime.now());
        entity.setJvmMetrics(metricsSnapshot);
        faultLogMapper.updateById(entity);
    }
}
