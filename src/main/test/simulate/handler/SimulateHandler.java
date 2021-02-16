package simulate.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.tooi.tcp.gateway.message.ProtoMsg;
import io.tooi.tcp.gateway.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Tooi
 * @since 2021-02-13 23:08:07
 */
@Slf4j
@ChannelHandler.Sharable
public class SimulateHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("发送登录消息");
        // 发送消息
        ProtoMsg.LoginRequest loginRequest = ProtoMsg.LoginRequest.newBuilder()
                .setSn("sn2001")
                .setToken("abc2001")
                .build();
        ProtoMsg.Message loginMessage = ProtoMsg.Message.newBuilder()
                .setType(ProtoMsg.MessageType.LOGIN_REQ)
                .setSessionId("sn2001")
                .setMessageId(CommonUtils.getUUID())
                .setLoginRequest(loginRequest)
                .build();
        ctx.writeAndFlush(loginMessage);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        log.error("发生异常", cause);
        ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof ProtoMsg.Message)) {
            super.channelRead(ctx, msg);
            return;
        }

        ProtoMsg.Message message = (ProtoMsg.Message) msg;
        ProtoMsg.MessageType type = message.getType();
        if (type == ProtoMsg.MessageType.LOGIN_RSP) {
            ProtoMsg.LoginResponse loginResponse = message.getLoginResponse();
            if (loginResponse.getResult()) {
                log.info("登录成功！");
                // 登录成功
                ProtoMsg.ReportRequest reportRequest = ProtoMsg.ReportRequest.newBuilder()
                        .setSn("sn2001")
                        .setBattery(12345)
                        .setHumidity(33.33)
                        .setLiquid(3.33)
                        .setTemperature(31.33)
                        .setLbs(98)
                        .setTimeStamp(System.currentTimeMillis())
                        .build();
                ProtoMsg.Message Datamessage = ProtoMsg.Message.newBuilder()
                        .setType(ProtoMsg.MessageType.REPORT_REQ)
                        .setMessageId(CommonUtils.getUUID())
                        .setSessionId(CommonUtils.getUUID())
                        .setReportRequest(reportRequest)
                        .build();

                new Thread(() -> {
                    while (true) {
                        try {
                            ctx.writeAndFlush(Datamessage);
                            log.debug("发送数据上报请求：{}", System.currentTimeMillis());
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }

        } else if (type.equals(ProtoMsg.MessageType.REPORT_RSP)) {
            // 数据上报响应
            log.info("收到数据上报响应：{}", System.currentTimeMillis());
        }


    }
}
