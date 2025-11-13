package com.item.sms.controller;

import com.item.sms.model.SmsReceiveDetail;
import com.item.sms.model.vo.SmsMessageVO;
import com.item.sms.service.SmsMessageService;
import com.item.sys.http.HttpResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 短信消息相关接口
 */
@RestController
@RequestMapping("/v1/message")
@RequiredArgsConstructor
public class SmsMessageController {

    private final SmsMessageService messageService;

    /**
     * 获取手机号码消息分组
     */
    @GetMapping("/getMessageGroup")
    public HttpResult getMessageGroup(@RequestParam(value = "deviceCode", required = false)
                                      String deviceCode,
                                      @RequestParam(value = "phone", required = false) String phone,
                                      @RequestParam(value = "pageNum", required = false,defaultValue = "1") Integer pageNum,
                                      @RequestParam(value = "pageSize", required = false,defaultValue = "10") Integer pageSize) {
        return HttpResult.ok(messageService.getMessageGroup(deviceCode,phone,pageNum,pageSize));
    }

    /**
     * 获取设备消息列表
     */
    @GetMapping("/getMessageList")
    public HttpResult getMessageList(@RequestParam(value = "deviceCode", required = false) String deviceCode,
                                     @RequestParam(value = "type", required = false) Integer type,
                                     @RequestParam(value = "phone", required = false) String phone,
                                     @RequestParam(value = "pageNum", required = false,defaultValue = "1") Integer pageNum,
                                     @RequestParam(value = "pageSize", required = false,defaultValue = "10") Integer pageSize) {
        return HttpResult.ok(messageService.getMessageList(deviceCode,type,phone,pageNum,pageSize));
    }

    /**
     * 获取手机号码聊天记录
     */
    @GetMapping("/getMessageRecord")
    public HttpResult getMessageRecord(@RequestParam(value = "phone") String phone,
                                       @RequestParam(value = "deviceCode") String deviceCode) {
        return HttpResult.ok(messageService.getMessageRecord(phone, deviceCode));
    }

    /**
     * 根据手机号码获取发送过的设备编号
     */
    @GetMapping("/getDeviceCodeByPhone")
    public HttpResult getDeviceCodeByPhone(@RequestParam(value = "phone") String phone) {
        return HttpResult.ok(messageService.getDeviceCodeByPhone(phone));
    }

    /**
     * 删除消息记录
     */
    @PostMapping("/deleteMessage")
    public HttpResult deleteMessage(@RequestBody List<SmsMessageVO> record) {
        return HttpResult.ok(messageService.deleteMessage(record));
    }

    /**
     * 获取未读消息数量
     */
    @RequestMapping("/getUnReadCount")
    public HttpResult getUnReadCount() {
        return HttpResult.ok(messageService.getUnReadCount());
    }

    /**
     * 通过任务id获取设备列表
     */
    @GetMapping("/getDeviceCodeById")
    public HttpResult getDeviceCodeById(@RequestParam String taskId) {
        return HttpResult.ok(messageService.getDeviceCodeById(taskId));
    }

    /**
     * 给回复的短信添加备注
     */
    @PostMapping("/addReplyRemark")
    public HttpResult addReplyRemark(@RequestBody SmsReceiveDetail record) {
        return HttpResult.ok(messageService.addReplyRemark(record));
    }
}
