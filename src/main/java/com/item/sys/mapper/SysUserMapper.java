package com.item.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.item.sys.model.SysUser;
import com.item.sys.model.vo.SysUserVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    @Select(
            "SELECT \n" +
                    "    a.*,\n" +
                    "    CASE \n" +
                    "        WHEN b.device_id IS NOT NULL THEN 1\n" +
                    "        ELSE 0\n" +
                    "    END AS isBind\n" +
                    "FROM sys_user a\n" +
                    "LEFT JOIN sys_user_device b \n" +
                    "    ON a.user_id = b.user_id\n" +
                    "    AND b.device_id =  #{deviceId};\n"
    )
    List<SysUserVO> getList(@Param("deviceId") String deviceId);

    @Select("SELECT * FROM sys_user WHERE account = #{account}")
    SysUser selectOne(@Param("account") String account);
}
