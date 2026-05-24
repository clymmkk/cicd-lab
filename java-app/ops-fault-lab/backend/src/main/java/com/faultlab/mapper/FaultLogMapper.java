package com.faultlab.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.faultlab.entity.FaultLogEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 故障记录 Mapper。
 */
@Mapper
public interface FaultLogMapper extends BaseMapper<FaultLogEntity> {
}
