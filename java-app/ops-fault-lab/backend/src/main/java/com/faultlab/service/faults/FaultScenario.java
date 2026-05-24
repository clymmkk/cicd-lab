package com.faultlab.service.faults;

import com.faultlab.common.model.FaultParam;
import com.faultlab.common.model.ScenarioStatus;
import com.faultlab.common.model.ScenarioSummary;

/**
 * 所有故障场景实现统一接口，便于通过控制器和前端统一编排。
 */
public interface FaultScenario {

    void trigger(FaultParam param);

    void stop();

    ScenarioStatus getStatus();

    String getPrinciple();

    String getCommands();

    String getCode();

    String getName();

    String getLevel();

    String getDescription();

    String getWarning();

    boolean isDangerous();

    boolean isStoppable();

    ScenarioSummary toSummary(boolean enabled);
}
