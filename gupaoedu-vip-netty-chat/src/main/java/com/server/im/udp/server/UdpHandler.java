package com.server.im.udp.server;

import com.gupaoedu.vip.netty.chat.protocol.IMMessage;
import com.gupaoedu.vip.netty.chat.protocol.IMP;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.msgpack.MessagePack;
import org.msgpack.MessageTypeException;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Consumer;

//https://www.jianshu.com/p/adc2de3691c7
@Slf4j
public class UdpHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//        ctx.close();
    }

    //接收Netty封装的DatagramPacket对象，然后构造响应消息
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
        log.info("UdpHandler channelRead0");
        ByteBuf byteBuf = packet.content();
        System.out.println("----" + byteBuf.readableBytes() + " " + packet.sender());
//        byteBuf.release()
        //利用ByteBuf的toString()方法获取请求消息
        String req = packet.content().toString(CharsetUtil.UTF_8);
        ctx.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(
                "谚语查询结果：" + req, CharsetUtil.UTF_8), packet.sender()));


//        System.out.println(req);
//        if("谚语字典查询?".equals(req)){
//            //创建新的DatagramPacket对象，传入返回消息和目的地址（IP和端口）
//            ctx.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(
//                    "谚语查询结果："+nextQuote(),CharsetUtil.UTF_8), packet.sender()));
//        }

//        ReferenceCountUtil.release(packet);
//        ByteBuf newBuf = ctx.alloc().buffer(1);
//        newBuf.writeByte(1);
//        ctx.fireChannelRead(newBuf);

//        ctx.fireUserEventTriggered()
    }


}
