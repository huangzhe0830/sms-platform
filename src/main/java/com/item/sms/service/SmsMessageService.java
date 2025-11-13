package com.item.sms.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.item.sms.mapper.SmsBatchTaskMapper;
import com.item.sms.mapper.SmsTaskDetailMapper;
import com.item.sms.model.SmsReceiveDetail;
import com.item.sms.model.SmsTaskDetail;
import com.item.sms.model.vo.SmsMessageRecordVO;
import com.item.sms.model.vo.SmsMessageVO;
import com.item.sms.utils.SnowFlake;
import com.item.socket.service.WebSocketManager;
import com.item.sys.mapper.SmsReceiveDetailMapper;
import com.item.sys.mapper.SysDeviceMapper;
import com.item.sys.mapper.SysUserDeviceMapper;
import com.item.sys.model.SysDevice;
import com.item.sys.model.SysUserDevice;
import com.item.sys.utils.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 短信消息服务
 */
@Service
public class SmsMessageService {

    private static final Logger log = LoggerFactory.getLogger(SmsMessageService.class);
    @Autowired
    private WebSocketManager webSocketManager;

    @Autowired
    private SmsTaskDetailMapper detailMapper;

    @Autowired
    private SmsBatchTaskMapper batchTaskMapper;

    @Autowired
    private SysDeviceMapper sysDeviceMapper;

    @Autowired
    private SysUserDeviceMapper sysUserDeviceMapper;

    @Autowired
    private SmsReceiveDetailMapper receiveDetailMapper;

    /**
     * 发送短信
     *
     * @return true 表示发送成功
     */
    public String sendSms(SmsTaskDetail task) {
        Map<String, Object> map = new HashMap<>();
        String phone = task.getPhone();
        String content = task.getContent();
        map.put("type", "send_sms");
        map.put("task_id", task.getId());
        map.put("phone_number", phone);
        map.put("content", content);
        map.put("device_code", task.getDeviceCode());
        map.put("sim_slot", 0);
        map.put("timestamp", task.getSendTime());
        try {
            log.info("{}发送短信：{}", task.getDeviceCode(), map);
            return webSocketManager.sendToClient(task.getDeviceCode(), JSONObject.toJSONString(map));
        } catch (Exception e) {
            return "sendToClient异常，发送失败";
        }
    }

    public void sendResult(JSONObject json) {
        String taskId = json.getString("task_id");
        String status = json.getString("status");
        String statusDesc = json.getString("status_desc");
        SmsTaskDetail smsTaskDetail = detailMapper.selectById(taskId);
        if (smsTaskDetail == null) {
            return;
        }
        smsTaskDetail.setStatus(status);
        smsTaskDetail.setStatusDesc(statusDesc);
        smsTaskDetail.setSendTime(LocalDateTime.now());
        detailMapper.updateById(smsTaskDetail);

        if ("success".equals(status)) {
            batchTaskMapper.incrementSentCount(smsTaskDetail.getBatchId());
        }
    }

    public void createDevice(JSONObject json) {
        String deviceCode = json.getString("device_code");
        SysDevice sysDevice = sysDeviceMapper.selectOne(deviceCode);
        if (sysDevice == null) {
            //创建
            sysDevice = new SysDevice();
            sysDevice.setDeviceId(SnowFlake.nextIdOfStringType());
            sysDevice.setDeviceCode(deviceCode);
            sysDevice.setCreateTime(LocalDateTime.now());
            sysDevice.setLastOnlineTime(LocalDateTime.now());
            sysDevice.setDefaultInterval(5); //默认五分钟
            sysDeviceMapper.insert(sysDevice);
        } else {
            sysDevice.setIsOnline(1);
            sysDevice.setLastOnlineTime(LocalDateTime.now());
            sysDeviceMapper.updateById(sysDevice);
        }
    }

    public PageInfo getMessageGroup(String deviceCode, String phone, Integer pageNum, Integer pageSize) {
        List<String> deviceCodes = null;
        if (!"admin".equals(SecurityUtils.getAccount()) || deviceCode != null) {
            deviceCodes = getDeviceCode(deviceCode);
            if (deviceCodes == null) {
                return new PageInfo();
            }
        }
        PageHelper.startPage(pageNum, pageSize);
        List<SmsMessageVO> messageGroup = detailMapper.getMessageGroup(deviceCodes, phone);
        return new PageInfo(messageGroup);
    }

    public PageInfo getMessageList(String deviceCode, Integer type, String phone, Integer pageNum, Integer pageSize) {
        List<String> deviceCodes = null;
        if (!"admin".equals(SecurityUtils.getAccount()) || deviceCode != null) {
            deviceCodes = getDeviceCode(deviceCode);
            if (deviceCodes == null) {
                return new PageInfo();
            }
        }
        PageHelper.startPage(pageNum, pageSize);
        List<SmsMessageVO> messageList = detailMapper.getMessageList(deviceCodes, type, phone);
        return new PageInfo(messageList);
    }

    public List<String> getDeviceCode(String deviceCode) {
        List<String> deviceCodes = new ArrayList<>();
        if (deviceCode == null) {
            //获取已绑定的设备
            String userId = SecurityUtils.getUserId();
            LambdaQueryWrapper<SysUserDevice> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysUserDevice::getUserId, userId);
            List<SysUserDevice> sysUserDevices = sysUserDeviceMapper.selectList(queryWrapper);
            if (sysUserDevices == null || sysUserDevices.size() == 0) {
                return null;
            }
            deviceCodes = sysUserDevices.stream()
                    .map(SysUserDevice::getDeviceCode)
                    .collect(Collectors.toList());
        } else {
            String[] code = deviceCode.split(",");
            for (String s : code) {
                deviceCodes.add(s);
            }
        }
        return deviceCodes;
    }

    @Transactional
    public List<SmsMessageRecordVO> getMessageRecord(String phone, String deviceCode) {
        //剔除手机号码区号 +86
        List<SmsMessageRecordVO> messageRecord = detailMapper.getMessageRecord(phone.replace("+86", ""), deviceCode);
        if (messageRecord != null && messageRecord.size() > 0) {
            //设置已读
            List<String> receiveIds = messageRecord.stream()
                    .filter(item -> item.getType() == 1)
                    .map(SmsMessageRecordVO::getId)
                    .collect(Collectors.toList());
            if (receiveIds.size() > 0) {
                for (String receiveId : receiveIds) {
                    SmsReceiveDetail receiveDetail = new SmsReceiveDetail();
                    receiveDetail.setStatus(1);
                    receiveDetail.setId(receiveId);
                    receiveDetailMapper.updateById(receiveDetail);
                }
            }
        }
        return messageRecord;
    }

    public void setDeviceOffline(String deviceCode) {
        UpdateWrapper<SysDevice> queryWrapper = new UpdateWrapper<>();
        queryWrapper.eq("device_code", deviceCode);
        sysDeviceMapper.update(new SysDevice(0), queryWrapper);
    }

    public void receiveSms(JSONObject json) {
        String deviceCode = json.getString("device_code");
        String phone = json.getString("sender_number");
        LocalDateTime date = Instant.ofEpochMilli(json.getLong("receive_time"))
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        String content = json.getString("content");
        Integer simSlot = json.getInteger("sim_slot");
        SmsReceiveDetail smsReceiveDetail = new SmsReceiveDetail();
        smsReceiveDetail.setId(SnowFlake.nextIdOfStringType());
        smsReceiveDetail.setDeviceCode(deviceCode);
        smsReceiveDetail.setPhone(phone);
        smsReceiveDetail.setReceiveTime(date);
        smsReceiveDetail.setContent(content);
        smsReceiveDetail.setSimSlot(simSlot);
        receiveDetailMapper.insert(smsReceiveDetail);
    }

    public List<String> getDeviceCodeByPhone(String phone) {
        return receiveDetailMapper.getDeviceCodeByPhone(phone);
    }

    @Transactional
    public int deleteMessage(List<SmsMessageVO> record) {
        //区分lastType
        List<String> receiveIds = record.stream()
                .filter(item -> item.getLastType() == 1)
                .map(SmsMessageVO::getId)
                .collect(Collectors.toList());
        List<String> sendIds = record.stream()
                .filter(item -> item.getLastType() == 0)
                .map(SmsMessageVO::getId)
                .collect(Collectors.toList());
        try {
            if (receiveIds.size() > 0) {
                receiveDetailMapper.deleteBatchIds(receiveIds);
            }
            if (sendIds.size() > 0) {
                detailMapper.deleteBatchIds(sendIds);
            }
        } catch (Exception e) {
            log.info("删除短信记录失败");
            return 0;
        }
        return 1;
    }

    public Long getUnReadCount() {
        String userId = SecurityUtils.getUserId();
        if ("admin".equals(SecurityUtils.getAccount())) {
            userId = null;
        }
        //获取设备
        List<SysDevice> list = sysDeviceMapper.getList(userId);
        if (list != null && list.size() > 0) {
            LambdaQueryWrapper<SmsReceiveDetail> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SmsReceiveDetail::getStatus, 0)
                    .in(SmsReceiveDetail::getDeviceCode, list.stream().map(SysDevice::getDeviceCode).collect(Collectors.toList()));
            return receiveDetailMapper.selectCount(queryWrapper);
        }
        return 0L;
    }

    public List<String> getDeviceCodeById(String taskId) {
        return detailMapper.getDeviceCodeById(taskId);
    }

    public int addReplyRemark(SmsReceiveDetail record) {
        return receiveDetailMapper.updateById(record);
    }
}
