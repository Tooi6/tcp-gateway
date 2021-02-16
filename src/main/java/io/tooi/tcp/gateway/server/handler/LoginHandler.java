package io.tooi.tcp.gateway.server.handler;

import com.sun.org.apache.xpath.internal.operations.Bool;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.tooi.tcp.gateway.concurrent.CallbackTask;
import io.tooi.tcp.gateway.concurrent.CallbackTaskScheduler;
import io.tooi.tcp.gateway.message.ProtoMsg;
import io.tooi.tcp.gateway.server.process.LoginProcesser;
import io.tooi.tcp.gateway.server.session.LocalSession;
import io.tooi.tcp.gateway.server.session.SessionManger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Tooi
 * @since 2021-02-12 11:43:00
 */
@Slf4j
@Service
@ChannelHandler.Sharable
public class LoginHandler extends ChannelInboundHandlerAdapter {

    @Autowired
    LoginProcesser loginProcesser;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (!(msg instanceof ProtoMsg.Message)) {
            super.channelRead(ctx, msg);
            return;
        }

        ProtoMsg.Message message = (ProtoMsg.Message) msg;
        ProtoMsg.MessageType type = message.getType();
        if (type.equals(ProtoMsg.MessageType.LOGIN_REQ)) {
            // 处理登录消息
            LocalSession localSession = new LocalSession(ctx.channel());

            CallbackTaskScheduler.add(new CallbackTask<Boolean>() {
                @Override
                public Boolean execute() throws Exception {
                    return loginProcesser.action(localSession, message);
                }

                @Override
                public void onBack(Boolean result) {
                    if (result) {
                        ctx.pipeline().remove(LoginHandler.this);
                        log.info("登录成功：{}", localSession.getSessionId());
                    } else {
                        SessionManger.getInstance().closeSession(ctx);
                        log.info("登录失败：{}", localSession.getSessionId());
                    }
                }

                @Override
                public void onException(Throwable t) {

                }
            });
        }

    }
}
