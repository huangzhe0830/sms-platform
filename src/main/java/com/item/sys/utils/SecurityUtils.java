package com.item.sys.utils;

import com.item.sys.security.JwtAuthenticatioToken;
import com.item.sys.security.JwtUserDetails;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import javax.servlet.http.HttpServletRequest;


/**
 * Security相关操作
 *
 * @author zp
 * @date Nov 20, 2018
 */
public class SecurityUtils {
    /**
     * 系统登录认证
     *
     * @param request
     * @param username
     * @param authenticationManager
     * @return
     */
    public static JwtAuthenticatioToken login(HttpServletRequest request, String username, AuthenticationManager authenticationManager) {
        JwtAuthenticatioToken token = new JwtAuthenticatioToken(username, null);
        token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        // 执行登录认证过程
        Authentication authentication = authenticationManager.authenticate(token);
        // 认证成功存储认证信息到上下文
        SecurityContextHolder.getContext().setAuthentication(authentication);
        // 生成令牌并返回给客户端
        token.setToken(JwtTokenUtils.generateToken(authentication));
        return token;
    }

    /**
     * 获取令牌进行认证
     *
     * @param request
     */
    public static void checkAuthentication(HttpServletRequest request) {
        // 获取令牌并根据令牌获取登录认证信息
        Authentication authentication = JwtTokenUtils.getAuthenticationeFromToken(request);
        // 设置登录认证信息到上下文
        if (authentication != null) {
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
    }

    /**
     * 获取当前用户名
     *
     * @return
     */
    public static String getAccount() {
        String account = null;
        JwtUserDetails jwtUserDetails = getJwtUserDetails();
        if (jwtUserDetails != null && jwtUserDetails.getAccount() != null) {
            account = jwtUserDetails.getAccount();
        }
        return account;
    }

    /**
     * 获取当前登录用户姓名
     *
     * @return
     */
    public static String getName() {
        String name = null;
        JwtUserDetails jwtUserDetails = getJwtUserDetails();
        if (jwtUserDetails != null && jwtUserDetails.getUsername() != null) {
            name = jwtUserDetails.getUsername();
        }
        return name;
    }

    /**
     * 获取用户主键
     *
     * @return
     */
    public static String getUserId() {
        String id = null;
        JwtUserDetails jwtUserDetails = getJwtUserDetails();
        if (jwtUserDetails != null && jwtUserDetails.getId() != null) {
            id = jwtUserDetails.getId();
        }
        return id;
    }

    /**
     * 获取jwt 用户详情
     *
     * @return
     */
    private static JwtUserDetails getJwtUserDetails() {
        JwtUserDetails jwtUserDetails = null;
        Authentication authentication = getAuthentication();
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal != null && principal instanceof JwtUserDetails) {
                jwtUserDetails = (JwtUserDetails) principal;
            }
        }
        return jwtUserDetails;
    }

    /**
     * 获取用户名
     *
     * @return
     */
    public static String getAccount(Authentication authentication) {
        String account = null;
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal != null && principal instanceof JwtUserDetails) {
                account = ((JwtUserDetails) principal).getAccount();
            }
        }
        return account;
    }

    /**
     * 获取用户主键
     *
     * @return
     */
    public static String getUserId(Authentication authentication) {
        String id = null;
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal != null && principal instanceof JwtUserDetails) {
                id = ((JwtUserDetails) principal).getId();
            }
        }
        return id;
    }

    /**
     * 获取当前登录信息
     *
     * @return
     */
    public static Authentication getAuthentication() {
        if (SecurityContextHolder.getContext() == null) {
            return null;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication;
    }

}
