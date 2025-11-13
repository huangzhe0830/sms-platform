package com.item.sys.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.item.sms.service.SmsReceiveService;
import com.item.sms.task.SmsTask;
import com.item.socket.service.WebSocketServer;
import com.item.sys.mapper.SysDeviceMapper;
import com.item.sys.model.SysDevice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class SysDeviceTask {

    @Autowired
    private SysDeviceMapper sysDeviceMapper;

    @Autowired
    private SmsReceiveService smsReceiveService;

    /**
     * 扫描设备队列，最后上线时间超过1分钟没更新 则设置离线
     */
    @Scheduled(fixedDelay = 60000)
    public void scanDeviceQueue() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.minusMinutes(1);

        // 查询1分钟未更新的在线设备
        List<SysDevice> timeoutDevices = sysDeviceMapper.selectList(
                new LambdaQueryWrapper<SysDevice>()
                        .eq(SysDevice::getIsOnline, 1) // 当前是在线状态
                        .lt(SysDevice::getLastOnlineTime, threshold) // 最后在线时间早于阈值
        );

        if (timeoutDevices.isEmpty()) {
            return;
        }

        // 批量更新为离线状态
        for (SysDevice device : timeoutDevices) {
            smsReceiveService.setDeviceOffline(device.getDeviceCode());
        }

        log.info("扫描设备队列完成，共{}个设备设置为离线", timeoutDevices.size());
    }
}
