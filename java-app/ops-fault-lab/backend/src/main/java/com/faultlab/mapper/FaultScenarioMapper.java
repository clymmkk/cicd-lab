package com.faultlab.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.faultlab.entity.FaultScenarioEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 场景配置 Mapper。
 */
@Mapper
public interface FaultScenarioMapper extends BaseMapper<FaultScenarioEntity> {
}
