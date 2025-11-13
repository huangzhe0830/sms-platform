package com.item.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.item.sys.http.HttpResult;
import com.item.sys.mapper.SysUserMapper;
import com.item.sys.model.SysUser;
import com.item.sys.security.JwtAuthenticatioToken;
import com.item.sys.service.SysUserService;
import com.item.sys.utils.PasswordUtils;
import com.item.sys.utils.SecurityUtils;
import com.item.sys.utils.SpringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public HttpResult<String> validPassword(String account, String password) {
        // 用户信息
        SysUser user = sysUserMapper.selectOne(account);
        if (user == null) {
            return HttpResult.error("用户不存在或密码错误");
        }

        // 验证密码，输错5次则锁定账户
        Integer pes = user.getExceptionCount();
        if (!PasswordUtils.matches(user.getSalt(), password, user.getPassword())) {
            Integer passwordErrors = 1;
            if (pes != null) {
                passwordErrors += pes;
            }
            if (passwordErrors >= 5) {
                return HttpResult.error("密码输入错误超限，账号已被锁定,请联系管理员!");
            } else {
                user.setExceptionCount(passwordErrors);
                sysUserMapper.updateById(user);
                return HttpResult.error("密码输入错误" + passwordErrors + "次，连续输入5次账号将被锁定!");
            }
        }

        // 输入密码正确将错误次数置0
        if (pes != null && pes > 0) {
            user.setExceptionCount(0);
            sysUserMapper.updateById(user);
        }

        return HttpResult.ok("");
    }

    @Override
    public HttpResult<String> loginAccount(HttpServletRequest request, String account) {
        // 用户信息
        LambdaQueryWrapper<SysUser> query = new LambdaQueryWrapper<>();
        query.eq(SysUser::getAccount, account);
        SysUser user = sysUserMapper.selectOne(query);

        // 账号不存在、密码错误
        if (user == null) {
            return HttpResult.error("账号不存在");
        }

        // 账号锁定
        if (user.getStatus() == 0) {
            return HttpResult.error("账号已被锁定,请联系管理员!");
        }

        // 系统登录认证
        AuthenticationManager authenticationManager = SpringUtils.getBean(AuthenticationManager.class);
        JwtAuthenticatioToken token = SecurityUtils.login(request, account, authenticationManager);

        return HttpResult.ok(token);
    }
}
