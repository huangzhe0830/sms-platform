package com.item.sys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.item.sys.model.SysDevice;
import com.item.sys.model.SysUserDevice;
import com.item.sys.model.dto.SysBindUsersDTO;

public interface SysDeviceService extends IService<SysDevice> {
    String bindUser(SysUserDevice record);

    String bindUsers(SysBindUsersDTO record);

    int deleteById(String deviceId);
}
