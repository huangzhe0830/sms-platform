package com.item.sys.controller;

import com.item.sys.http.HttpResult;
import com.item.sys.http.HttpResultEnum;
import com.item.sys.model.vo.LoginBean;
import com.item.sys.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Base64;

/**
 * 登录控制器
 */
@RestController
@RequestMapping("/v1")
public class SysLoginController {

    @Autowired
    private SysUserService sysUserService;

    /**
     * 登录接口
     */
    @PostMapping(value = "/login")
    public HttpResult login(@RequestBody LoginBean loginBean, HttpServletRequest request)  throws Exception {
        String account = loginBean.getAccount();
        if(StringUtils.isEmpty(account)){
            return HttpResult.error("请输入账号");
        }
        // 前端传base64编码密码
        String password = loginBean.getPassword();
        if(StringUtils.isEmpty(password)){
            return HttpResult.error("请输入密码");
        }
        // base64解码
        byte[] pwd = Base64.getDecoder().decode(password);
        if(pwd==null||pwd.length==0){
            return HttpResult.error("密码不正确");
        }
        password = new String(pwd);


        HttpResult<String> validedPassword = sysUserService.validPassword(account, password);
        if (!HttpResultEnum.SUCCESS.getCode().equals(validedPassword.getCode())) {
            return validedPassword;
        }

        return sysUserService.loginAccount(request, account);
    }

}
