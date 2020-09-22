package com.server.im.client.test;

import com.server.im.codec.IMEncoder;
import com.server.im.codec.MessageUtil;
import com.server.im.model.PkgInfo;
import com.server.im.model.WaitForFinish;
import com.server.im.udp.server.PkgManager;
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
import java.util.concurrent.TimeUnit;

public class Test implements Runnable {
    //MessageUnpacker
//    MessagePack
//    MessageBufferPacker

    PkgManager pkgManager = new PkgManager();
    private String meId;

    public Test(String id) {
        this.meId = id;
    }

    @Override
    public void run() {
        try {
            run(8888);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run(int port) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .option(ChannelOption.SO_SNDBUF, 2 * 1024 * 1024)//
                    .option(ChannelOption.SO_RCVBUF, 2 * 1024 * 1024)//
                    .handler(new TestHandler(pkgManager));
            Channel ch = b.bind(0).sync().channel();
            //向网段内的所有机器广播
            String data = "";
            while (data.getBytes().length <  (1358 * 1 + 0)) {
                data = data + "a";
            }
            System.out.println(data.getBytes().length);
//            InetSocketAddress to=new InetSocketAddress(
//                    "255.255.255.255", port);
            InetSocketAddress to = new InetSocketAddress(
                    "127.0.0.1", port);
            String toid = "";
            if (meId == MessageUtil.toid) {
                toid = MessageUtil.id;
            } else {
                toid = MessageUtil.toid;
            }

            PkgInfo pkgInfo = MessageUtil.buildLoginMsg("");
            pkgInfo.setFrom(meId);
            List<ByteBuf> datas = IMEncoder.encode(ch, pkgInfo);
            for (ByteBuf byteBuf : datas) {
                ch.write(new DatagramPacket(byteBuf, to));
            }
            ch.flush();

            TimeUnit.SECONDS.sleep(2);
//            System.out.println("meid=" + meId + "    toid=" + toid);
            pkgInfo = MessageUtil.buildMsg(toid, data);
            pkgInfo.setFrom(meId);
            datas = IMEncoder.encode(ch, pkgInfo);
            for (ByteBuf byteBuf : datas) {
//                ch.writeAndFlush(new DatagramPacket(byteBuf, to)).sync();
                ch.write(new DatagramPacket(byteBuf, to));
            }
            // TODO: 2020/9/21 需要一直发送PkgInfo.TYPE_PKG_FINISH直到收到 PkgInfo.TYPE_PKG_RECEIVE_FINISH
            ch.flush();
            pkgManager.addWaitForFinish(new WaitForFinish(to,ch.pipeline().lastContext(), pkgInfo));
            pkgManager.setPkgInfoConsumer((waitForFinish) -> {
                if (waitForFinish.send()) {

                } else {
                    pkgManager.removeWaitForFinish(waitForFinish);
                }
            });

            //客户端等待15s用于接收服务端的应答消息，然后退出并释放资源
            if (!ch.closeFuture().await(15000)) {
                System.out.println("查询超时！");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        int port = 8888;
        if (args != null && args.length > 0) {
            port = Integer.valueOf(args[0]);
        }
        new Thread(new Test(MessageUtil.toid)).start();
        new Thread(new Test(MessageUtil.id)).start();

//        System.out.println("for read");
//        System.in.read();

//        test();
//        System.out.println(UUID.randomUUID().toString());

    }

    private static void test() throws Exception {

        PkgManager pkgManager=new PkgManager();
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
        List<ByteBuf> ret = IMEncoder.encode(null, pkgInfo);

        System.out.println(pkgInfo.toString());
        System.out.println();
        ChannelInitializer channelInitializer = new ChannelInitializer() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ch.pipeline().addLast(new TestHandler(pkgManager));
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
