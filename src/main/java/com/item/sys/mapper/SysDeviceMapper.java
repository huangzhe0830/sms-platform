package com.item.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.item.sys.model.SysDevice;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysDeviceMapper extends BaseMapper<SysDevice> {

    @Select({
            "<script>",
            "SELECT a.* ",
            "FROM sys_device a ",
            "<where>",
            "  <if test='userId != null'>",
            " a.device_id in (select b.device_id from sys_user_device b where user_id = #{userId})",
            "  </if>",
            "</where>",
            "</script>"
    })
    List<SysDevice> getList(@Param("userId") String userId);

    @Select("SELECT * FROM sys_device WHERE device_code = #{deviceCode}")
    SysDevice selectOne(@Param("deviceCode") String deviceCode);

    @Delete("DELETE FROM sys_device WHERE device_id = #{deviceId}")
    int deleteById(@Param("deviceId") String deviceId);
}
