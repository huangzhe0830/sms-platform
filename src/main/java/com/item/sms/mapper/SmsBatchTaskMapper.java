package com.item.sms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.item.sms.model.SmsBatchTask;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SmsBatchTaskMapper extends BaseMapper<SmsBatchTask> {

    /**
     * 递增已发送短信数量
     */
    @Insert("update sms_batch_task set sent_count = sent_count + 1 where id = #{batchId}")
    void incrementSentCount(String batchId);
}
