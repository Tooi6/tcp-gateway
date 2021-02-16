package demo;

import io.tooi.tcp.gateway.message.ProtoMsg;
import io.tooi.tcp.gateway.utils.CommonUtils;
import org.apache.commons.codec.binary.Hex;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author Tooi
 * @since 2021-02-14 20:40:35
 */
public class Test {
    public static void main(String[] args) throws InterruptedException {
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
        System.out.println("登录请求：" + Hex.encodeHexString(loginMessage.toByteArray()));

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
        System.out.println("数据上报请求：" + Hex.encodeHexString(Datamessage.toByteArray()));
    }
}
