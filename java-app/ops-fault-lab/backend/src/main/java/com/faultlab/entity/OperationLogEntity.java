package com.faultlab.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 操作日志用于记录触发人、动作和来源 IP，方便审计和复盘。
 */
@Data
@TableName("operation_log")
public class OperationLogEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String user;

    private String action;

    private String ip;

    @TableField("create_time")
    private LocalDateTime createTime;
}
