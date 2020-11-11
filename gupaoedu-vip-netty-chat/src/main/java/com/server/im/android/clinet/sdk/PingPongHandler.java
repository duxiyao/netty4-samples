package com.server.im.android.clinet.sdk;

import com.server.im.android.clinet.sdk.codec.IMEncoder;
import com.server.im.android.clinet.sdk.codec.MessageUtil;
import com.server.im.android.clinet.sdk.model.PkgInfo;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import java.net.InetSocketAddress;
import java.util.List;

public class PingPongHandler extends ChannelInboundHandlerAdapter {
    private String meId;
    private InetSocketAddress to;

    public PingPongHandler(String id) {
        meId = id;
        to = new InetSocketAddress(
                "127.0.0.1", 8888);
    }

    public PingPongHandler(String id, String serverIp, int serverPort) {
        this.meId = id;
        to = new InetSocketAddress(
                serverIp, serverPort);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
//        System.out.println("userEventTriggered！");
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
//            System.out.println("event.state(): " + event.state());
            switch (event.state()) {
                case ALL_IDLE:
                    break;
                case READER_IDLE:
                    break;
                case WRITER_IDLE:
                    PkgInfo pkgInfo = MessageUtil.buildHeartBeatMsg("");
                    pkgInfo.setFrom(meId);
                    List<ByteBuf> datas = IMEncoder.encode(ctx.channel(), pkgInfo);
                    for (ByteBuf byteBuf : datas) {
                        ctx.write(new DatagramPacket(byteBuf, to));
                        break;
                    }
                    ctx.flush();
                    break;
                default:
                    break;
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }

}
