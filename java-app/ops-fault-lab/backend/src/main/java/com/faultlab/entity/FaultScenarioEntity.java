package com.faultlab.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 场景配置表保留后续从数据库调整默认参数和启停状态的能力。
 */
@Data
@TableName("fault_scenario")
public class FaultScenarioEntity {

    @TableId
    private Integer id;

    private String name;

    private String level;

    private String description;

    private String scenarioCode;

    private String params;

    private Boolean isEnabled;

    private Integer sortOrder;
}
