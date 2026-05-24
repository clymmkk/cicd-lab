package com.faultlab.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.faultlab.entity.OperationLogEntity;
import com.faultlab.mapper.OperationLogMapper;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 操作日志服务封装审计数据写入和查询。
 */
@Service
@RequiredArgsConstructor
public class OperationLogService {

    private final OperationLogMapper operationLogMapper;

    public void logAction(String user, String action, String ip) {
        OperationLogEntity entity = new OperationLogEntity();
        entity.setUser(user);
        entity.setAction(action);
        entity.setIp(ip);
        entity.setCreateTime(LocalDateTime.now());
        operationLogMapper.insert(entity);
    }

    public List<OperationLogEntity> listRecent(int limit) {
        LambdaQueryWrapper<OperationLogEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(OperationLogEntity::getCreateTime)
            .last("limit " + limit);
        return operationLogMapper.selectList(wrapper);
    }
}
