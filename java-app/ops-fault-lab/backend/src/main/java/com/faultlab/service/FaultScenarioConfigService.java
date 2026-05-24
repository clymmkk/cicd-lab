package com.faultlab.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.faultlab.common.exception.BizException;
import com.faultlab.common.model.FaultParam;
import com.faultlab.entity.FaultScenarioEntity;
import com.faultlab.mapper.FaultScenarioMapper;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 场景配置服务把数据库默认参数合并到请求中，便于后续做界面化调参。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FaultScenarioConfigService {

    private final FaultScenarioMapper faultScenarioMapper;
    private final ObjectMapper objectMapper;

    public List<FaultScenarioEntity> listAll() {
        LambdaQueryWrapper<FaultScenarioEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(FaultScenarioEntity::getSortOrder);
        return faultScenarioMapper.selectList(wrapper);
    }

    public FaultScenarioEntity requireEnabled(String scenarioCode) {
        FaultScenarioEntity entity = findByCode(scenarioCode).orElse(null);
        if (entity != null && Boolean.FALSE.equals(entity.getIsEnabled())) {
            throw new BizException("SCENARIO_DISABLED", "当前场景已被禁用");
        }
        return entity;
    }

    public FaultParam applyDefaults(FaultScenarioEntity entity, FaultParam requestParam) {
        FaultParam merged = requestParam == null ? new FaultParam() : requestParam.copy();
        Map<String, Object> mergedExtras = new HashMap<>(parseParams(entity));
        mergedExtras.putAll(merged.getExtras());
        merged.setExtras(mergedExtras);
        return merged;
    }

    public Optional<FaultScenarioEntity> findByCode(String scenarioCode) {
        LambdaQueryWrapper<FaultScenarioEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FaultScenarioEntity::getScenarioCode, scenarioCode);
        return Optional.ofNullable(faultScenarioMapper.selectOne(wrapper));
    }

    private Map<String, Object> parseParams(FaultScenarioEntity entity) {
        if (entity == null || entity.getParams() == null || entity.getParams().isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(entity.getParams(), new TypeReference<>() {
            });
        } catch (Exception exception) {
            log.warn("Failed to parse params for scenario {}", entity.getScenarioCode(), exception);
            return Collections.emptyMap();
        }
    }
}
