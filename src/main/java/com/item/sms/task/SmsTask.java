package com.item.sms.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.item.sms.mapper.SmsBatchTaskMapper;
import com.item.sms.mapper.SmsTaskDetailMapper;
import com.item.sms.model.SmsBatchTask;
import com.item.sms.model.SmsTaskDetail;
import com.item.sms.service.SmsMessageService;
import com.item.sys.mapper.SysDeviceMapper;
import com.item.sys.model.SysDevice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

/**
 * 短信发送执行器
 */
@Component
@Slf4j
public class SmsTask {

    @Autowired
    @Qualifier("smsTaskExecutor")
    private ExecutorService executorService;

    @Autowired
    private SmsTaskDetailMapper detailMapper;

    @Autowired
    private SmsBatchTaskMapper taskMapper;

    @Autowired
    private SmsMessageService smsMessageService;

    @Autowired
    private SysDeviceMapper deviceMapper;

    //设备级锁防止重复提交
    private final ConcurrentHashMap<String, Object> deviceLocks = new ConcurrentHashMap<>();

    /**
     * 定时任务，每30秒执行一次发送任务
     */
    @Scheduled(fixedDelay = 30000)
    public void scheduleSmsTasks() {
        // 获取在线设备
        LambdaQueryWrapper<SysDevice> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysDevice::getIsOnline, 1);
        List<SysDevice> onlineDevices = deviceMapper.selectList(queryWrapper);
        if (onlineDevices == null || onlineDevices.isEmpty()) {
            return;
        }
        for (SysDevice device : onlineDevices) {
            executorService.submit(() -> processDeviceQueue(device));
        }
    }

    /**
     * 定时任务 每3分钟执行一次，查询所有sending状态的任务，如果执行时间超过了3分钟则设置为失败
     */
    @Scheduled(cron = "0 0/3 * * * ?")
    public void checkSendingTasks() {
        List<SmsTaskDetail> tasks = detailMapper.selectList(
                new LambdaQueryWrapper<SmsTaskDetail>()
                        .eq(SmsTaskDetail::getStatus, "sending")
                        .orderByDesc(SmsTaskDetail::getSeqNo)
        );

        for (SmsTaskDetail task : tasks) {
            if (Duration.between(task.getExecutionTime(), LocalDateTime.now()).toMinutes() > 3) {
                task.setStatus("failed");
                task.setStatusDesc("执行时间过长");
                detailMapper.updateById(task);
            }
        }
    }

    /**
     * 扫描任务表 是否已完成全部任务 并设置状态已完成
     */
    @Scheduled(fixedDelay = 5000)
    public void scanTaskStatus() {
        List<SmsBatchTask> tasks = taskMapper.selectList(
                new QueryWrapper<SmsBatchTask>()
                        .eq("status", 1)
        );
        if (tasks == null || tasks.isEmpty()){
            return;
        }
        for (SmsBatchTask task : tasks) {
            //获取最后一个任务记录  如果不是等待发送 则设置完成
            LambdaQueryWrapper<SmsTaskDetail> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SmsTaskDetail::getBatchId, task.getId())
                    .orderByDesc(SmsTaskDetail::getSeqNo)
                    .last("LIMIT 1");
            SmsTaskDetail detail = detailMapper.selectOne(queryWrapper);
            if (detail == null){
                continue;
            }
            if (!"queued".equals(detail.getStatus())){
                task.setStatus(3);
                taskMapper.updateById(task);
            }
        }
    }

    /**
     * 处理设备队列
     * @param device
     */
    private void processDeviceQueue(SysDevice device) {
        String deviceCode = device.getDeviceCode();
        Object lock = deviceLocks.computeIfAbsent(deviceCode, k -> new Object());
        synchronized (lock) {
            doProcess(device);
        }
    }

    /**
     * 处理设备队列
     * @param device
     */
    private void doProcess(SysDevice device) {
        String deviceCode = device.getDeviceCode();
        // 查找该设备最早未完成的任务
        SmsBatchTask task = taskMapper.selectOne(
                new LambdaQueryWrapper<SmsBatchTask>()
                        .like(SmsBatchTask::getDeviceIds, device.getDeviceId())
                        .eq(SmsBatchTask::getStatus, 1)
                        .orderByAsc(SmsBatchTask::getCreateTime)
                        .last("LIMIT 1")
        );
        if (task == null) {
            return;
        }
        // 查找该设备第一条待发送任务（状态=queued）
        SmsTaskDetail nextTask = detailMapper.selectOne(
                new LambdaQueryWrapper<SmsTaskDetail>()
                        .eq(SmsTaskDetail::getDeviceCode, deviceCode)
                        .eq(SmsTaskDetail::getBatchId, task.getId())
                        .in(SmsTaskDetail::getStatus, "queued")
                        .orderByAsc(SmsTaskDetail::getSeqNo)
                        .last("LIMIT 1")
        );
        if (nextTask == null) return;

        // 检查是否到达间隔时间
        SmsTaskDetail lastSent = detailMapper.selectOne(
                new LambdaQueryWrapper<SmsTaskDetail>()
                        .eq(SmsTaskDetail::getDeviceCode, deviceCode)
                        .eq(SmsTaskDetail::getBatchId, task.getId())
                        .in(SmsTaskDetail::getStatus, "success", "failed", "sending")
                        .orderByDesc(SmsTaskDetail::getSeqNo)
                        .last("LIMIT 1")
        );
        // 获取间隔分钟
        Integer intervalMinutes = task.getIntervalMinutes();
        LocalDateTime now = LocalDateTime.now();

        // 检查是否正在发送
        if (lastSent != null && "sending".equals(lastSent.getStatus())) {
            return;
        }

        // 检查是否到达间隔时间
        if (lastSent != null && lastSent.getSendTime() != null) {
            long diffSeconds = Duration.between(lastSent.getSendTime(), now).getSeconds();
            long intervalSeconds = intervalMinutes * 60L;
            if (diffSeconds < intervalSeconds) {
                return; // 未到间隔
            }
        }


        // 执行发送
        try {
            String ret = smsMessageService.sendSms(nextTask);
            if (!"1".equals(ret)) {
                nextTask.setStatus("failed");
                nextTask.setStatusDesc(ret);
                nextTask.setSendTime(now);
                nextTask.setExecutionTime(now);
                detailMapper.updateById(nextTask);
            } else {
                nextTask.setStatus("sending");
                nextTask.setExecutionTime(now);
                nextTask.setStatusDesc("发送中");
                detailMapper.updateById(nextTask);
            }
        } catch (Exception e) {
            //TODO 异常也要更新状态并考虑重试
            nextTask.setStatus("failed");
            nextTask.setStatusDesc("异常:" + e.getMessage());
            nextTask.setSendTime(now);
            nextTask.setExecutionTime(now);
            detailMapper.updateById(nextTask);
        }
    }

    /**
     * 移除设备锁
     * @param deviceCode
     */
    public void removeDeviceLock(String deviceCode) {
        if (deviceLocks.remove(deviceCode) != null) {
            log.info("设备 {} 下线，已移除设备锁", deviceCode);
        } else {
            log.info("设备 {} 下线时未找到锁对象", deviceCode);
        }
    }
}
