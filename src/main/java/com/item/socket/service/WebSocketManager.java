package com.item.socket.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 连接管理器
 * 用于维护 deviceCode <-> Session 的映射
 * 由 WebSocketServer 调用进行注册、注销、发送
 */
@Component
public class WebSocketManager {

    /**
     * 保存 deviceCode -> Session 的映射
     */
    private static final ConcurrentHashMap<String, Session> CLIENTS = new ConcurrentHashMap<>();
    private static final Logger log = LoggerFactory.getLogger(WebSocketManager.class);

    /**
     * 注册客户端连接
     */
    public void addClient(String deviceCode, Session session) {
        CLIENTS.put(deviceCode, session);
    }

    /**
     * 移除客户端连接
     */
    public void removeClient(String deviceCode) {
        CLIENTS.remove(deviceCode);
    }

    /**
     * 按 deviceCode 发送消息
     */
    public String sendToClient(String deviceCode, String message) {
        Session session = CLIENTS.get(deviceCode);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
                log.info("发送成功[" + deviceCode + "]：" + message);
                return "1";
            } catch (IOException e) {
                log.info("发送失败[" + deviceCode + "]：" + e.getMessage());
                return "发送失败[" + deviceCode + "]：" + e.getMessage();
            }
        } else {
            return "未找到 deviceCode=" + deviceCode + " 的连接";
        }
    }

    /**
     * 群发
     */
    public void broadcast(String message) {
        CLIENTS.values().forEach(session -> {
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 获取当前在线客户端数
     */
    public int getOnlineCount() {
        return CLIENTS.size();
    }

    /**
     * 获取所有在线 deviceCode
     */
    public Set<String> getAllDeviceCodes() {
        return CLIENTS.keySet();
    }
}