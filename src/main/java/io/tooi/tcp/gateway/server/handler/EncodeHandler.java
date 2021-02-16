package io.tooi.tcp.gateway.server.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.tooi.tcp.gateway.message.ProtoMsg;

/**
 * @author Tooi
 * @since 2021-02-09 10:57:16
 */
public class EncodeHandler extends MessageToByteEncoder {
    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        // 发送字节码消息
        if (msg instanceof ProtoMsg.Message) {
            ProtoMsg.Message message = (ProtoMsg.Message) msg;
            out.writeBytes(message.toByteArray());
        }
    }
}
