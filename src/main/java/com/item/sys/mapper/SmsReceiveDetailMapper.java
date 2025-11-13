package com.item.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.item.sms.model.SmsReceiveDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SmsReceiveDetailMapper extends BaseMapper<SmsReceiveDetail> {

    @Select("SELECT device_code FROM `sms_task_detail` where phone = #{phone} GROUP BY device_code\n" +
            "UNION \n" +
            "select device_code from sms_receive_detail where phone = #{phone} GROUP BY device_code")
    List<String> getDeviceCodeByPhone(@Param("phone") String phone);
}
