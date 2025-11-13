package com.item.sys.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 设备信息表
 */
@Data
@TableName("sys_device")
public class SysDevice {

    /** 设备id */
    @TableId(value = "device_id", type = IdType.INPUT)
    private String deviceId;

    /** 设备sn码 */
    @TableField("device_code")
    private String deviceCode;

    /** 备注 */
    @TableField("remark")
    private String remark;

    /** 默认短信推送间隔（min） */
    @TableField("default_interval")
    private Integer defaultInterval;

    /** 是否在线 1是 0否 */
    @TableField("is_online")
    private Integer isOnline;

    /** 最后在线时间 */
    @TableField("last_online_time")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastOnlineTime;

    /** 创建时间 */
    @TableField("create_time")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @TableField(exist = false)
    private Long userId;

    public SysDevice(Integer isOnline) {
        this.isOnline = isOnline;
    }
    public SysDevice() {
    }
}
