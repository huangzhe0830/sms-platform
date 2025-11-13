package com.item.sms.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 短信任务明细表
 * 每一条短信对应一条记录
 */
@Data
public class SmsTaskDetail {
    /** 主键ID */
    private String id;

    /** 所属批次ID */
    private String batchId;

    /** 设备编号 */
    private String deviceCode;

    /** 目标手机号 */
    private String phone;

    /** 手机卡槽 */
    private Integer simSlot;

    /** 短信内容 */
    private String content;

    /** 实际发送时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private LocalDateTime sendTime;

    /** 任务执行时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private LocalDateTime executionTime;

    /** 发送状态（success / failed / queued / sending ） */
    private String status;

    /** 状态说明 */
    private String statusDesc;

    /** 任务顺序 */
    private Integer seqNo;
}