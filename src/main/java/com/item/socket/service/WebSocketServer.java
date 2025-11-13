package com.item.socket.service;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONValidator;
import com.item.sms.service.SmsReceiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;


/**
 * WebSocket 服务端
 * 用于接收客户端连接、消息、并能主动推送消息
 */
@ServerEndpoint("/ws")
@Component
public class WebSocketServer {

    /**
     * 存储当前连接的客户端
     */
    private static final CopyOnWriteArraySet<WebSocketServer> webSocketSet = new CopyOnWriteArraySet<>();

    /**
     * 当前连接的 session
     */
    private Session session;

    /**
     * 当前客户端的唯一标识
     */
    private String deviceCode;

    /**
     * WebSocketManager 用于管理连接
     */
    private static WebSocketManager wsManager;

    /**
     * SmsReceiveService 用于处理收到的业务消息
     */
    private static SmsReceiveService receiveService;

    /**
     * 通过 Spring 注入静态变量（因为 @ServerEndpoint 不是 Spring 管理的 Bean）
     */
    @Autowired
    public void setWebSocketManager(WebSocketManager manager) {
        WebSocketServer.wsManager = manager;
    }

    @Autowired
    public void setSmsReceiveService(SmsReceiveService service) {
        WebSocketServer.receiveService = service;
    }

    /**
     * 建立连接
     */
    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        webSocketSet.add(this);
        System.out.println("客户端连接成功！当前在线人数：" + webSocketSet.size());
        sendMessage("连接成功，deviceCode=" + deviceCode);
    }

    /**
     * 关闭连接
     */
    @OnClose
    public void onClose() {
        if (this.deviceCode != null) {
            wsManager.removeClient(this.deviceCode);
            webSocketSet.remove(this);
        }
        //设置设备离线状态
        receiveService.setDeviceOffline(this.deviceCode);
        System.out.println("客户端[" + deviceCode + "]断开连接，当前在线人数：" + wsManager.getOnlineCount());
    }

    /**
     * 收到消息
     */
    @OnMessage
    public void onMessage(String message) {
        System.out.println("收到[" + deviceCode + "]消息：" + message);
        String reply = "消息异常";
        if (message == null || "".equals(message) || !isValidJson(message)) {
            sendMessage(reply);
            return;
        }
        //判断是否是心跳，是的话则存储设备编号
        JSONObject json = JSONObject.parseObject(message);
        String deviceCode = json.getString("device_code");
        if (deviceCode != null) {
            this.deviceCode = deviceCode;
            wsManager.addClient(this.deviceCode, this.session);
        }
        reply = receiveService.receive(message);
        sendMessage(reply);
    }

    /**
     * 错误处理
     */
    @OnError
    public void onError(Session session, Throwable error) {
        System.err.println("WebSocket 错误[" + deviceCode + "]：" + error.getMessage());
    }

    /**
     * 给当前客户端发消息
     */
    public void sendMessage(String message) {
        try {
            this.session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            System.err.println("发送失败[" + deviceCode + "]：" + e.getMessage());
        }
    }

    public boolean isValidJson(String message) {
        return JSONValidator.from(message).validate();
    }
}