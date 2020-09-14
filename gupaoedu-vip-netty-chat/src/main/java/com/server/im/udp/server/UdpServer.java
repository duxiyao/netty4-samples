package com.server.im.udp.server;

import com.gupaoedu.vip.netty.chat.udp.server.ChineseProverbServer;
import com.gupaoedu.vip.netty.chat.udp.server.ChineseProverbServerHandler;
import com.server.im.codec.IMDecoder;
import com.server.im.codec.IMEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;

public class UdpServer {

    private StateManager stateManager=new StateManager();
    private PkgManager pkgManager=new PkgManager();
    public void run(int port) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        try {
            //通过NioDatagramChannel创建Channel，并设置Socket参数支持广播
            //UDP相对于TCP不需要在客户端和服务端建立实际的连接，因此不需要为连接（ChannelPipeline）设置handler
            Bootstrap b = new Bootstrap();
            b.group(bossGroup)
                    .channel(NioDatagramChannel.class)
//                    .option(ChannelOption.SO_RCVBUF, 20 * 1024 * 1024)//定义接收缓冲区大小，太小的话超过这个大小的数据包接收不全
                    .option(ChannelOption.SO_RCVBUF, 1 * 1024 * 1024)//定义接收缓冲区大小，太小的话超过这个大小的数据包接收不全
//                    .option(ChannelOption.SO_SNDBUF, 2 * 1024 * 1024)//
                    .option(ChannelOption.SO_SNDBUF, 1472)//
//                    .option(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator(2048,10000,165535))   //定义每次能读取到的数据包大小，数据包超过这个大小需要分次读取
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(new ChannelInitializer<NioDatagramChannel>() {
                        @Override
                        protected void initChannel(NioDatagramChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
//                            p.addLast(new StateHandler(stateManager));
//                            p.addLast(new UdpHandler());
                            p.addLast(new IMDecoder(stateManager,pkgManager));
                            //客户端进行心跳
//                            p.addLast(new IdleStateHandler(5,0,0));
//                            p.addLast(new PingPongHandler());
                        }
                    });
//                    .handler(new UdpHandler());//!!!
            b.bind(port).sync().channel().closeFuture().await();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        int port = 8888;
        if (args != null && args.length > 0) {
            port = Integer.valueOf(args[0]);
        }
        new UdpServer().run(port);
    }

}
