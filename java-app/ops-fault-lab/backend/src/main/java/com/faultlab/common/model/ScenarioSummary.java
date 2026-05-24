package com.faultlab.common.model;

import lombok.Builder;
import lombok.Data;

/**
 * 场景摘要一次返回前端所需的大部分展示数据，减少额外接口往返。
 */
@Data
@Builder
public class ScenarioSummary {

    private String code;
    private String name;
    private String level;
    private String description;
    private String warning;
    private boolean dangerous;
    private boolean stoppable;
    private boolean enabled;
    private String principle;
    private String commands;
    private ScenarioStatus status;
}
