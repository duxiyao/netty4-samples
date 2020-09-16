package com.server.im.client.test;

import com.server.im.codec.IMEncoder;
import com.server.im.model.PkgInfo;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.UUID;

public class Test {
    //MessageUnpacker
//    MessagePack
//    MessageBufferPacker

    public void run(int port) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .option(ChannelOption.SO_SNDBUF, 2 * 1024 * 1024)//
                    .option(ChannelOption.SO_RCVBUF, 2 * 1024 * 1024)//
                    .handler(new TestHandler());
            Channel ch = b.bind(0).sync().channel();
            //向网段内的所有机器广播
            String data = "";
            while (data.getBytes().length < 1472) {
                data = data + "abc";
            }
            System.out.println(data.getBytes().length);
            ch.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(
                    data, CharsetUtil.UTF_8), new InetSocketAddress(
                    "255.255.255.255", port))).sync();
            //客户端等待15s用于接收服务端的应答消息，然后退出并释放资源
            if (!ch.closeFuture().await(1000)) {
                System.out.println("查询超时！");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
//        int port = 8888;
//        if (args != null && args.length > 0) {
//            port = Integer.valueOf(args[0]);
//        }
//        new Test().run(port);


        test();
    }

    private static void test() throws Exception {

        String data = "";
        while (data.getBytes().length < (1358 * 2 + 1)) {
            data = data + "ab";
        }
        PkgInfo pkgInfo = new PkgInfo();
        pkgInfo.setFrom(UUID.randomUUID().toString());
        pkgInfo.setTo(UUID.randomUUID().toString());
        pkgInfo.setPkgId(UUID.randomUUID().toString());
        pkgInfo.setType((byte) 1);
        pkgInfo.setVersion((byte) 1);
        pkgInfo.addData(data.getBytes(IMEncoder.CODE));
        List<ByteBuf> ret = IMEncoder.encode(null,pkgInfo);

        System.out.println(pkgInfo.toString());
        System.out.println();
        ChannelInitializer channelInitializer = new ChannelInitializer() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ch.pipeline().addLast(new TestHandler());
            }
        };
        EmbeddedChannel channel = new EmbeddedChannel(channelInitializer);

        InetSocketAddress inetSocketAddress = new InetSocketAddress("127.0.0.1", 80);
        for (ByteBuf byteBuf : ret) {
            channel.writeInbound(new DatagramPacket(byteBuf, inetSocketAddress));
//            System.out.println(byteBuf.refCnt());
        }
    }
}
