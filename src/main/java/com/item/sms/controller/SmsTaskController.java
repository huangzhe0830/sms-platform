package com.item.sms.controller;

import com.item.sms.mapper.SmsBatchTaskMapper;
import com.item.sms.mapper.SmsTaskDetailMapper;
import com.item.sms.model.SmsBatchTask;
import com.item.sms.model.SmsTaskDetail;
import com.item.sms.model.dto.SmsRequestDTO;
import com.item.sms.service.SmsMessageService;
import com.item.sms.service.SmsTaskService;
import com.item.sms.utils.SnowFlake;
import com.item.sys.http.HttpResult;
import com.item.sys.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 提供消息任务接口
 */
@RestController
@RequestMapping("/v1/sms")
@RequiredArgsConstructor
public class SmsTaskController {

    private final SmsTaskService smsTaskService;

    private final SmsBatchTaskMapper batchTaskMapper;

    private final SmsMessageService smsMessageService;

    private final SmsTaskDetailMapper smsTaskDetailMapper;

    /**
     * 创建批量短信发送任务
     */
    @PostMapping("/createBatch")
    public HttpResult createBatch(@RequestBody SmsRequestDTO dto) {
        dto.setUserId(SecurityUtils.getUserId());
        smsTaskService.createBatch(dto);
        return HttpResult.ok("任务创建成功，系统将按间隔自动发送");
    }

    /**
     * 修改正在进行中的批次任务的发送间隔
     */
    @PostMapping("/updateInterval")
    public HttpResult updateBatchInterval(@RequestParam Long taskId, @RequestParam int newIntervalMinutes) {
        smsTaskService.updateBatchInterval(taskId, newIntervalMinutes);
        return HttpResult.ok("批次间隔修改成功，未发送任务已重新调度");
    }

    /**
     * 修改任务
     */
    @PostMapping("/updateTask")
    public HttpResult updateTask(SmsBatchTask task) {
        return HttpResult.ok(batchTaskMapper.updateById(task));
    }

    @PostMapping("/pause/{batchId}")
    public HttpResult pauseBatch(@PathVariable String batchId) {
        smsTaskService.pauseBatch(batchId);
        return HttpResult.ok("任务已暂停");
    }

    @PostMapping("/resume/{batchId}")
    public HttpResult resumeBatch(@PathVariable String batchId) {
        smsTaskService.resumeBatch(batchId);
        return HttpResult.ok("任务已继续");
    }

    @PostMapping("/delete/{batchId}")
    public HttpResult deleteBatch(@PathVariable String batchId) {
        smsTaskService.deleteBatch(batchId);
        return HttpResult.ok("任务已删除");
    }

    /**
     * 发送消息
     */
    @PostMapping("/sendSms")
    public HttpResult sendSms(@RequestBody SmsTaskDetail dto) {
        dto.setId(SnowFlake.nextIdOfStringType());
        dto.setSendTime(LocalDateTime.now());
        dto.setStatus("sending");
        smsTaskDetailMapper.insert(dto);
        return HttpResult.ok(smsMessageService.sendSms(dto));
    }

    /**
     * 获取任务列表
     */
    @GetMapping("/getTaskList")
    public HttpResult getTaskList(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                  @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        return HttpResult.ok(smsTaskService.getTaskList(SecurityUtils.getUserId(),pageNum,pageSize));
    }

    /**
     * 任务详情
     */
    @GetMapping("/getTaskDetail")
    public HttpResult getTaskDetail(@RequestParam String taskId,
                                    @RequestParam(value = "deviceCode", required = false) String deviceCode,
                                    @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                    @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        return HttpResult.ok(smsTaskService.getTaskDetail(taskId,deviceCode, pageNum, pageSize));
    }
}