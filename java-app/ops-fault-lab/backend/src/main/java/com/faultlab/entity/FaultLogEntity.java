package com.faultlab.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 故障记录保存触发、恢复和崩溃瞬间的关键快照。
 */
@Data
@TableName("fault_log")
public class FaultLogEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("fault_type")
    private String faultType;

    private String operator;

    @TableField("trigger_time")
    private LocalDateTime triggerTime;

    @TableField("recovery_time")
    private LocalDateTime recoveryTime;

    @TableField("jvm_metrics")
    private String jvmMetrics;

    private String status;
}
