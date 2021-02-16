package io.tooi.tcp.gateway.server.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.tooi.tcp.gateway.message.ProtoMsg;
import io.tooi.tcp.gateway.queue.CacheQueue;
import io.tooi.tcp.gateway.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author Tooi
 * @since 2021-02-12 11:46:00
 */
@Slf4j
@Service
@ChannelHandler.Sharable
public class DataReportHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof ProtoMsg.Message)) {
            super.channelRead(ctx, msg);
            return;
        }

        ProtoMsg.Message message = (ProtoMsg.Message) msg;
        ProtoMsg.MessageType type = message.getType();
        if (type.equals(ProtoMsg.MessageType.REPORT_REQ)) {
            // 数据上报消息
            ProtoMsg.ReportRequest reportRequest = message.getReportRequest();
            CacheQueue.up2MQQueue.put(message);
            log.debug("收到数据上报消息：{}", reportRequest);

            ProtoMsg.ReportResponse reportResponse = ProtoMsg.ReportResponse.newBuilder()
                    .setResult(true)
                    .setTimeStamp(System.currentTimeMillis())
                    .build();
            ProtoMsg.Message reportMessage = ProtoMsg.Message.newBuilder()
                    .setType(ProtoMsg.MessageType.REPORT_RSP)
                    .setMessageId(CommonUtils.getUUID())
                    .setSessionId("-1")
                    .setReportResponse(reportResponse)
                    .build();
            ctx.writeAndFlush(reportMessage);
        }
    }
}
