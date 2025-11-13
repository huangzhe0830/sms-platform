package com.item.sys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.item.sys.http.HttpResult;
import com.item.sys.model.SysUser;

import javax.servlet.http.HttpServletRequest;

public interface SysUserService extends IService<SysUser> {

    /**
     * 校验密码
     *
     * @param account
     * @param password
     * @return
     */
    HttpResult<String> validPassword(String account, String password);

    /**
     * 账号登录到系统
     *
     * @param request
     * @param account
     * @return
     */
    HttpResult<String> loginAccount(HttpServletRequest request, String account);
}
