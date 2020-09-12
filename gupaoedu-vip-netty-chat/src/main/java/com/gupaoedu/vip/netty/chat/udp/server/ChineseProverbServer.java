package com.gupaoedu.vip.netty.chat.udp.server;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

/**
 * 入门netty udp
 */
public class ChineseProverbServer {
    public void run(int port)throws Exception{
        EventLoopGroup bossGroup=new NioEventLoopGroup();
        try
        {
            //通过NioDatagramChannel创建Channel，并设置Socket参数支持广播
            //UDP相对于TCP不需要在客户端和服务端建立实际的连接，因此不需要为连接（ChannelPipeline）设置handler
            Bootstrap b=new Bootstrap();
            b.group(bossGroup)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
//                    .handler(new ChannelInitializer<NioDatagramChannel>() {
//                        @Override
//                        protected void initChannel(NioDatagramChannel ch) throws Exception {
//                            ChannelPipeline p = ch.pipeline();
//                            p.addLast(new ChineseProverbServerHandler());
//                        }
//                    });//???
                    .handler(new ChineseProverbServerHandler());//???
            b.bind(port).sync().channel().closeFuture().await();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally{
            bossGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args)throws Exception
    {
        int port=8888;
        if (args!=null&&args.length>0)
        {
            port=Integer.valueOf(args[0]);
        }
        new ChineseProverbServer().run(port);
    }
}
