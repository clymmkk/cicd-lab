package com.faultlab.common.model;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

/**
 * 场景状态用于驱动前端卡片、监控面板和恢复操作。
 */
@Data
@Builder
public class ScenarioStatus {

    private String code;
    private String name;
    private String state;
    private boolean running;
    private boolean stoppable;
    private String message;
    private String warning;
    private LocalDateTime startedAt;
    private LocalDateTime updatedAt;
    @Builder.Default
    private Map<String, Object> metrics = new LinkedHashMap<>();
}
