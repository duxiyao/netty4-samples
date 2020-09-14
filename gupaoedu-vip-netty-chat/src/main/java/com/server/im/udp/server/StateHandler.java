package com.server.im.udp.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Deprecated
@Slf4j
public class StateHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    private StateManager stateManager;

    public StateHandler(StateManager stateManager) {
        this.stateManager = stateManager;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        if (!stateManager.contains(msg.sender())) {
            InetSocketAddress inetSocketAddress = msg.sender();
            String req = "【服务器】您好," + msg.sender().toString();
            log.info(req);
            ctx.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(req, CharsetUtil.UTF_8), msg.sender()));

//          需要在decoder中进行 parse and add
        }
//        ByteBuf byteBuf = msg.content();
//        ReferenceCountUtil.release(msg);
//        ByteBuf newBuf = ctx.alloc().buffer(1);
//        newBuf.writeByte(1);
//        ctx.fireChannelRead(byteBuf);
        ctx.fireChannelRead(msg);
//        final Channel channel = ctx.channel();

//        channelGroup.forEach(ch -> {
//            if (channel != ch) {
//                ch.writeAndFlush(channel.remoteAddress() + " 发送的消息:" + "msg" + " \n");
//            } else {
//                ch.writeAndFlush(" 【自己】" + "msg" + " \n");
//            }
//        });
    }

    //保留所有与服务器建立连接的channel对象，这边的GlobalEventExecutor在写博客的时候解释一下，看其doc
//    private static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

//    //tcp 表示服务端与客户端连接建立
//    @Override
//    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
//        log.info("handlerAdded");
//        Channel channel = ctx.channel();  //其实相当于一个connection
//
//        /**
//         * 调用channelGroup的writeAndFlush其实就相当于channelGroup中的每个channel都writeAndFlush
//         *
//         * 先去广播，再将自己加入到channelGroup中
//         */
//        channelGroup.writeAndFlush(" 【服务器】 -" + channel.remoteAddress() + " 加入\n");
//        channelGroup.add(channel);
//    }

//    //tcp
//    @Override
//    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
//        log.info("handlerRemoved");
//        Channel channel = ctx.channel();
//        channelGroup.writeAndFlush(" 【服务器】 -" + channel.remoteAddress() + " 离开\n");
//
//        //验证一下每次客户端断开连接，连接自动地从channelGroup中删除调。
//        System.out.println(channelGroup.size());
//        //当客户端和服务端断开连接的时候，下面的那段代码netty会自动调用，所以不需要人为的去调用它
//        //channelGroup.remove(channel);
//    }

    //连接处于活动状态
//    @Override
//    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        log.info("channelActive");
//
//        Channel channel = ctx.channel();
//        System.out.println(channel.remoteAddress() + " 上线了");
//        ctx.fireChannelActive();
//    }

//    @Override
//    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
//        log.info("channelInactive");
//        Channel channel = ctx.channel();
//        System.out.println(channel.remoteAddress() + " 下线了");
//    }

}
