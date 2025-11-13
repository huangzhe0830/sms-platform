package com.item.sys.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.item.sys.model.SysUser;
import com.item.sys.model.vo.JwtUser;
import com.item.sys.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * 用户登录认证信息查询
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private SysUserService sysUserService;

    @Override
    public UserDetails loadUserByUsername(String account) throws UsernameNotFoundException {
        LambdaQueryWrapper<SysUser> query = new LambdaQueryWrapper<>();
        SysUser user = sysUserService.getOne(query.eq(SysUser::getAccount, account));
        if (user == null) {
            throw new UsernameNotFoundException("该用户不存在");
        }
        Set<String> permissions = new HashSet<>();
        List<GrantedAuthority> grantedAuthorities = permissions.stream().map(GrantedAuthorityImpl::new).collect(Collectors.toList());
        return new JwtUserDetails(user.getUserId(), user.getAccount(), user.getName(), user.getPassword(), user.getSalt(), grantedAuthorities);
    }
}