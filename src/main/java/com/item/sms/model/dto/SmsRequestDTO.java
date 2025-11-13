package com.item.sms.model.dto;

import lombok.Data;

import java.util.List;

/**
 * 请求参数封装
 */
@Data
public class SmsRequestDTO {
    private String userId;
    /** 批次名称 */
    private String batchName;
    /** 设备id */
    private String deviceIds;
    /** 设备编号 */
    private String deviceCodes;
    /** 短信内容 */
    private String content;
    /** 手机号列表 */
    private String phones;
    /** 发送间隔（分钟） */
    private int intervalMinutes;
    /** 类型"batchSequential" 或 "perDeviceQueue" */
    private String type;

}