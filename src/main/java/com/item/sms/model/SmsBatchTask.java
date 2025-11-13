package com.item.sms.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 批量短信任务主表
 * 一次批量发送对应一个批次
 */
@Data
public class SmsBatchTask {
    /** 主键ID */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private String id;

    /** 用户id */
    private String userId;

    /** 批次名称，用于展示和区分 */
    private String batchName;

    /** 设备id */
    private String deviceIds;

    /** 短信内容 */
    private String content;

    /** 每条短信发送间隔（分钟） */
    private Integer intervalMinutes;

    /** 总任务数 */
    private Integer totalCount;

    /** 已发送任务数 */
    private Integer sentCount;

    /** 状态：0-待执行，1-执行中，2-暂停，3-已完成，4-已删除 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private LocalDateTime updateTime;
}
