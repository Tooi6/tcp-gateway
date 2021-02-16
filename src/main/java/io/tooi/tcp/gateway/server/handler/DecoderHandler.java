package io.tooi.tcp.gateway.server.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.tooi.tcp.gateway.message.ProtoMsg;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 解码器
 *
 * @author Tooi
 * @since 2021-02-09 10:51:08
 */
@Slf4j
public class DecoderHandler extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 标记一些当前的readIndex
        in.markReaderIndex();
        int length = in.readableBytes();

        byte[] messageBytes;
        if (in.hasArray()) {
            ByteBuf slice = in.slice();
            messageBytes = slice.array();
        } else {
            messageBytes = new byte[length];
            in.readBytes(messageBytes, 0, length);
        }
        ProtoMsg.Message outMsg = ProtoMsg.Message.parseFrom(messageBytes);
        if (outMsg != null) {
            out.add(outMsg);
        }
    }
}
