package com.item.sms.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SmsMessageRecordVO {

    private String id;

    private String deviceCode;

    private String phone;

    private String content;

    private Integer simSlot;

    private LocalDateTime smsTime;

    /**
     * 类型 1接 0发
     */
    private Integer type;

    private String status;
}
