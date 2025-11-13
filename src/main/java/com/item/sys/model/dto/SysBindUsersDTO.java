package com.item.sys.model.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

@Data
public class SysBindUsersDTO {

    /** 用户id */
    private String userIds;

    /** 设备id */
    private String deviceId;

    /** 设备编号 */
    private String deviceCode;
}
