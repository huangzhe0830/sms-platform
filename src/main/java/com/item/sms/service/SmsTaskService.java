package com.item.sms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.item.sms.mapper.SmsBatchTaskMapper;
import com.item.sms.mapper.SmsTaskDetailMapper;
import com.item.sms.model.SmsBatchTask;
import com.item.sms.model.SmsTaskDetail;
import com.item.sms.model.dto.SmsRequestDTO;
import com.item.sms.utils.SnowFlake;
import com.item.sys.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 任务业务逻辑层
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SmsTaskService {

    private final SmsBatchTaskMapper batchMapper;
    private final SmsTaskDetailMapper detailMapper;

    /**
     * 创建批次任务
     */
    @Transactional
    public void createBatch(SmsRequestDTO dto) {
        LocalDateTime now = LocalDateTime.now();
        String batchName = dto.getBatchName();
        String content = dto.getContent();
        String[] phones = dto.getPhones().split("\\n");
        int intervalMinutes = dto.getIntervalMinutes();
        // 多个设备，按逗号分隔
        String[] deviceIds = dto.getDeviceIds().split(",");
        String[] deviceCodes = dto.getDeviceCodes().split(",");

        // 1. 保存批次信息到数据库
        SmsBatchTask batch = new SmsBatchTask();
        batch.setId(String.valueOf(new Date().getTime()));
        batch.setUserId(dto.getUserId());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        batch.setBatchName(batchName == null ? sdf.format(new Date()) : batchName);
        batch.setDeviceIds(dto.getDeviceIds());
        batch.setContent(content);
        batch.setIntervalMinutes(intervalMinutes);
        batch.setTotalCount(phones.length);
        batch.setStatus(1);
        batch.setCreateTime(now);
        batchMapper.insert(batch);

        // 2. 计算每个设备分配的手机号数量
        int totalPhones = phones.length;
        int totalDevices = deviceIds.length;
        int avgCount = totalPhones / totalDevices;
        int remainder = totalPhones % totalDevices;

        List<SmsTaskDetail> details = new ArrayList<>();
        int phoneIndex = 0;
        // 3. 为每个设备平均分配手机号
        for (int d = 0; d < totalDevices; d++) {
            String deviceCode = (deviceCodes.length > d ? deviceCodes[d] : null);

            // 当前设备要处理的手机号数量
            int assignCount = avgCount + (d < remainder ? 1 : 0);

            //获取设备未完成的任务序号
            Integer lastSeq = detailMapper.getLastSeqNo(deviceCode);
            int seq = lastSeq != null ? lastSeq+1 : 1;
            for (int j = 0; j < assignCount && phoneIndex < totalPhones; j++, phoneIndex++) {
                String phone = phones[phoneIndex];

                try {
                    SmsTaskDetail detail = new SmsTaskDetail();
                    detail.setId(SnowFlake.nextIdOfStringType());
                    detail.setBatchId(batch.getId());
                    detail.setDeviceCode(deviceCode);
                    detail.setPhone(phone);
                    detail.setSimSlot(0);
                    detail.setContent(content);
                    detail.setSendTime(null);
                    detail.setStatus("queued");
                    detail.setStatusDesc("待发送");
                    detail.setSeqNo(seq++);
                    details.add(detail);

                } catch (Exception e) {
                    log.error("创建批次任务失败：{}", e.getMessage());
                }
            }
        }

        // 4. 批量入库
        if (!details.isEmpty()) {
            detailMapper.insertBatch(details);
        }
    }

    public PageInfo getTaskList(String userId,Integer pageNum, Integer pageSize) {
        if (userId == null) {
            return new PageInfo();
        }
        if ("admin".equals(SecurityUtils.getAccount())){
            userId = null;
        }
        PageHelper.startPage(pageNum, pageSize);
        LambdaQueryWrapper<SmsBatchTask> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(userId!=null,SmsBatchTask::getUserId, userId)
                //过滤删除状态
                .ne(SmsBatchTask::getStatus, 4)
                .orderByDesc(SmsBatchTask::getCreateTime);
        return new PageInfo(batchMapper.selectList(queryWrapper));
    }

    public PageInfo getTaskDetail(String taskId, String deviceCode, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        LambdaQueryWrapper<SmsTaskDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SmsTaskDetail::getBatchId, taskId)
                .like(deviceCode!=null,SmsTaskDetail::getDeviceCode, deviceCode)
                .orderByAsc(SmsTaskDetail::getSeqNo);
        return new PageInfo(detailMapper.selectList(queryWrapper));
    }

    /**
     * 动态修改批次任务间隔（重新调度未发送任务）
     */
    @Transactional
    public void updateBatchInterval(Long batchId, int newIntervalMinutes) {
        SmsBatchTask batch = batchMapper.selectById(batchId);
        if (batch == null) {
            throw new IllegalArgumentException("无效的批次ID：" + batchId);
        }

        // 更新批次表的间隔字段
        batch.setIntervalMinutes(newIntervalMinutes);
        batchMapper.updateById(batch);
    }

    @Transactional
    public void pauseBatch(String batchId) {
        SmsBatchTask batch = batchMapper.selectById(batchId);
        if (batch == null) throw new RuntimeException("批次不存在");

        batch.setStatus(2); // 暂停
        batchMapper.updateById(batch);
    }

    @Transactional
    public void resumeBatch(String batchId) {
        SmsBatchTask batch = batchMapper.selectById(batchId);
        if (batch == null) throw new RuntimeException("批次不存在");

        batch.setStatus(1); // 执行中
        batchMapper.updateById(batch);
    }

    @Transactional
    public void deleteBatch(String batchId) {
        SmsBatchTask batch = batchMapper.selectById(batchId);
        if (batch == null) throw new RuntimeException("批次不存在");
        // 更新批次状态为已删除
        batch.setStatus(4);
        batchMapper.updateById(batch);

        // 删除数据库所有任务
        detailMapper.delete(
                new QueryWrapper<SmsTaskDetail>()
                        .eq("batch_id", batchId)
        );
    }

}