package com.server.im.client.test;

import com.server.im.codec.IMDecoder;
import com.server.im.model.PkgInfo;
import com.server.im.udp.server.PkgManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;

public class TestHandler extends
        SimpleChannelInboundHandler<DatagramPacket> {

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable
            cause) throws Exception {
        cause.printStackTrace();
//        ctx.close();
    }

    PkgManager pkgManager=new PkgManager();
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {

        ByteBuf byteBuf = msg.content();

        PkgInfo pkgInfo=IMDecoder.decode(byteBuf,pkgManager);
        System.out.println(pkgInfo.toString());
//        System.out.println("read:--"+byteBuf.toString(CharsetUtil.UTF_8));
//        System.out.println("read:--"+byteBuf.getByte(0));
//        ctx.close();
    }
}