package com.item.sms.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SmsReceiveDetail {

    private String id;

    /** 设备编号 */
    private String deviceCode;

    /** 目标手机号 */
    private String phone;

    /** 手机卡槽 */
    private Integer simSlot;

    /** 短信内容 */
    private String content;

    /** 接收时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private LocalDateTime receiveTime;

    /** 短信状态 1已读 0未读*/
    private Integer status;

    /** 备注*/
    private String remark;

    public SmsReceiveDetail() {
    }

    public SmsReceiveDetail(String id ,Integer status){
        this.id = id;
        this.status = status;
    }
}
