package com.item.sys.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.item.sys.http.HttpResult;
import com.item.sys.mapper.SysDeviceMapper;
import com.item.sys.model.SysDevice;
import com.item.sys.model.SysUserDevice;
import com.item.sys.model.dto.SysBindUsersDTO;
import com.item.sys.service.SysDeviceService;
import com.item.sys.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 设备相关接口
 */
@RestController
@RequestMapping("/v1/device")
public class SysDeviceController {

    @Autowired
    private SysDeviceService deviceService;

    @Autowired
    private SysDeviceMapper deviceMapper;

    @PostMapping(value = "/bindUser")
    public HttpResult bindUser(@RequestBody SysUserDevice record) {
        return HttpResult.ok(deviceService.bindUser(record));
    }

    @PostMapping(value = "/bindUsers")
    public HttpResult bindUsers(@RequestBody SysBindUsersDTO record) {
        return HttpResult.ok(deviceService.bindUsers(record));
    }

    @GetMapping(value = "/list")
    public HttpResult list(@RequestParam(value = "userId", required = false)
                               String userId) {
        userId = userId == null ? SecurityUtils.getUserId() : userId;
        if ("admin".equals(SecurityUtils.getAccount())) {
            userId = null;
        }
        return HttpResult.ok(deviceMapper.getList(userId));
    }

    @PostMapping(value = "/update")
    public HttpResult update(@RequestBody SysDevice record) {
        return HttpResult.ok(deviceMapper.updateById(record));
    }

    @PostMapping(value = "/delete")
    public HttpResult delete(@RequestParam String deviceId) {
        return HttpResult.ok(deviceService.deleteById(deviceId));
    }

}
