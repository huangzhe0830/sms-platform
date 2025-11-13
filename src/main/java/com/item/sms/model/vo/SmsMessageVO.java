package com.item.sms.model.vo;

import lombok.Data;

@Data
public class SmsMessageVO {

    private String id;

    /**
     * 状态 0未读 1已读
     */
    private Integer status;

    private String deviceCode;

    private String phone;

    /**
     * 最新一条短信
     */
    private String lastMessage;

    /**
     * 最新一条短信发送时间
     */
    private String lastTime;

    /**
     * 手机号码共几条记录
     */
    private Integer msgNum;

    /**
     * 最新一条消息是发还是收 1收 0发
     */
    private Integer lastType;

    private String remark;
}
