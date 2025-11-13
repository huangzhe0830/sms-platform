package com.item.sms.service;

import com.alibaba.fastjson.JSONObject;
import com.item.sms.task.SmsTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 短信平台接收服务
 */
@Service
@Slf4j
public class SmsReceiveService {

    @Autowired
    private SmsMessageService smsMessageService;

    @Autowired
    private SmsTask smsTaskScheduler;

    public String receive(String message) {
        log.info("收到短信平台消息：{}", message);
        JSONObject json = JSONObject.parseObject(message);
        String type = json.getString("type");
        if (type == null){
            return null;
        }
        Map<String, Object> map = new HashMap<>();
        switch (type) {
            case "heartbeat":
                //判断是否注册
                smsMessageService.createDevice(json);
                //直接回复
                map.put("type", "heartbeat_ack");
                map.put("server_time", new Date().getTime());
                break;
            case "send_result":
                smsMessageService.sendResult(json);
                break;
            case "receive_sms":
                smsMessageService.receiveSms(json);
                break;
        }
        String reply = JSONObject.toJSONString(map);
        log.info("回复短信平台消息：{}", reply);
        return reply;
    }

    public void setDeviceOffline(String deviceCode) {
        smsMessageService.setDeviceOffline(deviceCode);
        smsTaskScheduler.removeDeviceLock(deviceCode); // 删除锁
    }
}
