package com.item.sys.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.item.sms.utils.SnowFlake;
import com.item.sys.http.HttpResult;
import com.item.sys.mapper.SysUserDeviceMapper;
import com.item.sys.mapper.SysUserMapper;
import com.item.sys.model.SysDevice;
import com.item.sys.model.SysUser;
import com.item.sys.model.SysUserDevice;
import com.item.sys.model.dto.SysUserPassUpdate;
import com.item.sys.service.SysUserService;
import com.item.sys.utils.PasswordUtils;
import net.sf.jsqlparser.statement.select.Offset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/user")
public class SysUserController {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysUserService sysUserService;

    @PostMapping(value = "/create")
    public HttpResult create(@RequestBody SysUser record) {
        record.setPassword("sms@321!"); // 初始密码sms@321!
        String salt = PasswordUtils.getSalt();
        // 新增用户
        LambdaQueryWrapper<SysUser> query = new LambdaQueryWrapper<>();
        query.eq(SysUser::getAccount, record.getAccount());
        if (sysUserMapper.selectOne(query) != null) {
            return HttpResult.error("用户名已存在!");
        }
        String password = PasswordUtils.encode(record.getPassword(), salt);
        record.setUserId(SnowFlake.nextIdOfStringType());
        record.setSalt(salt);
        record.setPassword(password);
        return HttpResult.ok(sysUserMapper.insert(record));
    }

    /**
     * 用户列表
     */
    @GetMapping(value = "/list")
    public HttpResult list(@RequestParam String deviceId) {
        return HttpResult.ok(sysUserMapper.getList(deviceId));
    }

    /**
     * 修改密码
     */
    @PostMapping(value = "/updateLoginPass")
    public HttpResult updateLoginPass(@RequestBody SysUserPassUpdate model) throws Exception {
        SysUser record = sysUserMapper.selectById(Long.parseLong(model.getId()));
        //匹配输入的密码对不对
        if (!PasswordUtils.matches(record.getSalt(), model.getPagePassword(), record.getPassword())) {
            return HttpResult.ok("原密码不正确");
        }
        String salt = PasswordUtils.getSalt();
        String updatePassword = PasswordUtils.encode(model.getNewPassword(), salt);
        record.setSalt(salt);
        record.setPassword(updatePassword);
        return HttpResult.ok(sysUserService.updateById(record));
    }

    /**
     * 修改用户信息
     */
    @PutMapping(value = "/update")
    public HttpResult update(@RequestBody SysUser record) {
        return HttpResult.ok(sysUserMapper.updateById(record));
    }
}
