package com.item.sys.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户设备绑定表
 */
@Data
@TableName("sys_user_device")
public class SysUserDevice {

    /** 主键id */
    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    /** 用户id */
    @TableField("user_id")
    private String userId;

    /** 设备id */
    @TableField("device_id")
    private String deviceId;

    /** 设备编号 */
    @TableField("device_code")
    private String deviceCode;

    /** 绑定时间 */
    @TableField(value = "bind_time", fill = FieldFill.INSERT)
    private LocalDateTime bindTime;
}