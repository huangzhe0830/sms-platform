package com.item.sms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.item.sms.model.SmsTaskDetail;
import com.item.sms.model.vo.SmsMessageRecordVO;
import com.item.sms.model.vo.SmsMessageVO;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Mapper
public interface SmsTaskDetailMapper extends BaseMapper<SmsTaskDetail> {
    /**
     * 批量插入
     */
    @Insert({
            "<script>",
            "INSERT INTO sms_task_detail (id,batch_id,device_code,sim_slot, phone, content, send_time, status, seq_no) VALUES ",
            "<foreach item='item' collection='list' separator=','>",
            "(#{item.id},#{item.batchId},#{item.deviceCode},#{item.simSlot}, #{item.phone}, #{item.content}, #{item.sendTime}, #{item.status},#{item.seqNo})",
            "</foreach>",
            "</script>"
    })
    void insertBatch(@Param("list") List<SmsTaskDetail> details);

    /**
     * 获取手机号码消息列表
     */
    @Select({
            "<script>",
            "SELECT device_code      AS deviceCode,",
            "       phone,",
            "       content          AS lastMessage,",
            "       send_time        AS lastTime,",
            "       COUNT(1)         AS msgNum,",
            "       0                AS lastType",
            "FROM sms_task_detail",
            "WHERE status = 'success'",
            "<if test='list != null and list.size() > 0'>",
            "  AND device_code IN",
            "  <foreach item='item' collection='list' open='(' separator=',' close=')'>",
            "    #{item}",
            "  </foreach>",
            "</if>",
            "<if test='phone != null'>",
             "  AND phone LIKE CONCAT('%', #{phone}, '%')",
            "</if>",
            "GROUP BY phone",
            "ORDER BY MAX(execution_time) DESC",
            "</script>"
            })
    List<SmsMessageVO> getMessageGroup(@Param("list") List<String> deviceCodes,@Param("phone") String phone);

    @Select({
            "<script>",
            "select  id,status,deviceCode,phone,lastMessage,lastTime,lastType,remark \n" +
                    "from (\n" +
                    "SELECT id,1 as status,device_code as deviceCode,\n" +
                    "                    phone,content as lastMessage,\n" +
                    "                    send_time as lastTime,\n" +
                    "                    0 as lastType,'' as remark FROM `sms_task_detail` \n" +
                    "                    where status = 'success'  \n" +
                    "<if test='list != null and list.size() > 0'>",
                    "  AND device_code IN",
                    "  <foreach item='item' collection='list' open='(' separator=',' close=')'>",
                    "    #{item}",
                    "  </foreach>",
                    "</if>",
                    "                    UNION ALL \n" +
                    "select id,status,device_code as deviceCode,\n" +
                    "                    phone,content as lastMessage,  \n" +
                    "                    receive_time as lastTime, \n" +
                    "                    1 as lastType,remark FROM `sms_receive_detail`" +
                     "<if test='list != null and list.size() > 0'>",
                    "  where device_code IN",
                    "  <foreach item='item' collection='list' open='(' separator=',' close=')'>",
                    "    #{item}",
                    "  </foreach>",
                    "</if>",
                    "                     )AS combined where 1=1" +
                    "<if test='type != null'>",
                        "and lastType = #{type}",
                    "</if>",
                    "<if test='phone != null'>",
                        "AND phone LIKE CONCAT('%', #{phone}, '%')",
                    "</if>",
                    "                    ORDER BY status asc,lastTime desc",
            "</script>"
    })
    List<SmsMessageVO> getMessageList(@Param("list") List<String> deviceCodes,@Param("type") Integer type ,@Param("phone") String phone);

    @Select({
            "SELECT id,device_code,phone, sim_slot, content, smsTime, status,type\n" +
                    "FROM (\n" +
                    "    SELECT id,device_code,phone, sim_slot, content, send_time AS smsTime, status,0 as type\n" +
                    "    FROM sms_task_detail\n" +
                    "    WHERE phone like CONCAT('%', #{phone}, '%') and device_code = #{deviceCode}" +
                    "\n" +
                    "    UNION ALL\n" +
                    "\n" +
                    "    SELECT id,device_code,phone, sim_slot, content, receive_time AS smsTime, 'success' AS status,1 as type\n" +
                    "    FROM sms_receive_detail\n" +
                    "    WHERE phone like CONCAT('%', #{phone}, '%') and device_code = #{deviceCode}\n" +
                    ") AS combined\n" +
                    "ORDER BY smsTime;"
    })
    List<SmsMessageRecordVO> getMessageRecord(String phone, String deviceCode);

    @Select("SELECT MAX(send_time) FROM sms_task_detail WHERE batch_id = #{batchId} AND status IN ('success', 'sending')")
    LocalDateTime selectLastSentTime(@Param("batchId") Long batchId);

    @Update({
            "<script>",
            " <foreach collection='list' item='item' separator=';'> ",
            "   UPDATE sms_task_detail ",
            "   SET send_time = #{item.sendTime} ",
            "   WHERE id = #{item.id} ",
            " </foreach> ",
            "</script>"
    })
    void batchUpdateSendTime(@Param("list") List<SmsTaskDetail> list);

    @Select("select send_time from sms_task_detail where `status` = 'queued' \n" +
            "and batch_id in (select id from sms_batch_task where device_code = #{deviceCode} and `status` in (0,1,2))\n" +
            "and send_time > NOW()\n" +
            "order by send_time desc limit 1 ")
    LocalDateTime getLastTaskDate(@Param("deviceCode") String deviceCode);

    @Select("select max(seq_no) from sms_task_detail a,sms_batch_task b where a.batch_id = b.id and device_code =  #{deviceCode} and a.status in ('queued','sending') and b.status <> 4")
    Integer getLastSeqNo(String deviceCode);

    @Select("select device_code from sms_task_detail where batch_id = #{taskId} GROUP BY device_code ")
    List<String> getDeviceCodeById(String taskId);
}
