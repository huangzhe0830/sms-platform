package com.item.sys.model.vo;

import com.item.sys.model.SysUser;
import lombok.Data;

@Data
public class SysUserVO extends SysUser {
    /** 是否绑定设备 1是 0否 */
    private Integer isBind;
}
