package com.item.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.item.sms.utils.SnowFlake;
import com.item.sys.mapper.SysDeviceMapper;
import com.item.sys.mapper.SysUserDeviceMapper;
import com.item.sys.model.SysDevice;
import com.item.sys.model.SysUserDevice;
import com.item.sys.model.dto.SysBindUsersDTO;
import com.item.sys.service.SysDeviceService;
import com.item.sys.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
public class SysDeviceServiceImpl extends ServiceImpl<SysDeviceMapper, SysDevice> implements SysDeviceService {

    @Autowired
    private SysUserDeviceMapper userDeviceMapper;

    @Override
    public String bindUser(SysUserDevice record) {
        try {
            record.setBindTime(LocalDateTime.now());
            record.setId(SnowFlake.nextIdOfStringType());
            userDeviceMapper.insert(record);
            return "1";
        } catch (Exception e) {
            log.error("用户设备绑定失败", e);
            return "账号已绑定本设备，请勿重复绑定";
        }
    }

    @Override
    public String bindUsers(SysBindUsersDTO record) {
        if (record.getUserIds() == null || "".equals(record.getUserIds())){
            userDeviceMapper.delete(new LambdaQueryWrapper<SysUserDevice>().eq(SysUserDevice::getDeviceId, record.getDeviceId()));
            return "1";
        }
        //先删除关联表中的设备
        userDeviceMapper.delete(new LambdaQueryWrapper<SysUserDevice>().eq(SysUserDevice::getDeviceId, record.getDeviceId()));
        String[] userIds = record.getUserIds().split(",");
        for (String userId : userIds) {
            SysUserDevice sysUserDevice = new SysUserDevice();
            sysUserDevice.setUserId(userId);
            sysUserDevice.setDeviceId(record.getDeviceId());
            sysUserDevice.setDeviceCode(record.getDeviceCode());
            bindUser(sysUserDevice);
        }
        return "1";
    }

    @Override
    @Transactional
    public int deleteById(String deviceId) {
        //先删除关联表
        userDeviceMapper.delete(new LambdaQueryWrapper<SysUserDevice>().eq(SysUserDevice::getDeviceId, deviceId));
        return baseMapper.deleteById(deviceId);
    }
}
