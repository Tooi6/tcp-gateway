package io.tooi.tcp.gateway.server.process;

import io.tooi.tcp.gateway.message.ProtoMsg;
import io.tooi.tcp.gateway.queue.CacheQueue;
import io.tooi.tcp.gateway.server.session.LocalSession;
import io.tooi.tcp.gateway.server.session.SessionManger;
import io.tooi.tcp.gateway.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * @author Tooi
 * @since 2021-02-14 12:34:02
 */
@Slf4j
@Component("loginProcesser")
public class LoginProcesser {

    @Autowired
    SessionManger sessionManger;


    public Boolean action(LocalSession localSession, ProtoMsg.Message message) {
        ProtoMsg.LoginRequest loginRequest = message.getLoginRequest();
        // 验证token
        if (null == loginRequest.getToken()) {
            ProtoMsg.LoginResponse loginResponse = ProtoMsg.LoginResponse.newBuilder()
                    .setResult(false)
                    .setTimeStamp(System.currentTimeMillis())
                    .build();
            ProtoMsg.Message response = ProtoMsg.Message.newBuilder()
                    .setType(ProtoMsg.MessageType.LOGIN_RSP)
                    .setMessageId(CommonUtils.getUUID())
                    .setSessionId("-1")
                    .setLoginResponse(loginResponse)
                    .build();
            localSession.writeAndFlush(response);
            return false;
        }

        /**
         * 绑定Session
         */
        localSession.bind();
        sessionManger.addLocalSession(localSession);

        try {
            CacheQueue.up2MQQueue.put(message);
        } catch (InterruptedException e) {
            log.error("登录缓冲消息失败", e);
        }

        /**
         * 响应登录成功
         */
        ProtoMsg.LoginResponse loginResponse = ProtoMsg.LoginResponse.newBuilder()
                .setResult(true)
                .setTimeStamp(System.currentTimeMillis())
                .build();
        ProtoMsg.Message response = ProtoMsg.Message.newBuilder()
                .setType(ProtoMsg.MessageType.LOGIN_RSP)
                .setMessageId(localSession.getSessionId())
                .setSessionId("-1")
                .setLoginResponse(loginResponse)
                .build();
        localSession.writeAndFlush(response);
        return true;
    }

}
