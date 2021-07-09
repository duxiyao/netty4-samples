package com.server.im.android.clinet.sdk;

import com.server.im.android.clinet.sdk.impl.Chat;
import com.server.im.android.clinet.sdk.impl.Login;
import com.server.im.android.clinet.sdk.manager.PkgManager;
import com.server.im.android.clinet.sdk.model.PkgInfo;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.net.InetSocketAddress;
import java.util.concurrent.Callable;

// TODO: 2020/9/23 在安卓上需要启动另外进程

public class IMSdk {
    private static class Inner {
        private static IMSdk instance = new IMSdk();
    }

    private IMSdk() {

    }

    public static IMSdk getInstance() {
        return Inner.instance;
    }

    private String uid;
//    private String serverIp = "127.0.0.1";
//    private String serverIp = "47.94.102.169";
    private String serverIp = "server.dancecode.cn";
    private int serverPort = 60000;
    private IMLogin imLogin;
    private IMChat imChat;
    PkgManager pkgManager;
    private InetSocketAddress server;
    private Runnable chatRun;
    private volatile boolean logined = false;
    private IMOnReceive imOnReceive;
    private IMLoginCallback imLoginCallback;

    public void init(String uid, Runnable chatCallable,IMLoginCallback imLoginCallback) {
        this.imLoginCallback=imLoginCallback;
        this.chatRun = chatCallable;
        this.uid = uid;
        pkgManager = new PkgManager();
        // : 2020/9/21 需要一直发送PkgInfo.TYPE_PKG_FINISH直到收到 PkgInfo.TYPE_PKG_RECEIVE_FINISH
        pkgManager.setPkgInfoConsumer((waitForFinish) -> {
            if (waitForFinish.send()) {
//                System.out.println("发送 waitForFinish.send()");
            } else {
                System.out.println("removeWaitForFinish(waitForFinish)");
                pkgManager.removeWaitForFinish(waitForFinish);
            }
        });
        server = new InetSocketAddress(serverIp, serverPort);
        new Thread(() -> {
            try {
                initClient();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public IMChat getChat() {
        return imChat;
    }

    public IMLogin getLogin() {
        return imLogin;
    }

    public String getUid() {
        return uid;
    }

    private void initClient() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {

            ChannelInitializer channelInitializer = new ChannelInitializer() {
                @Override
                protected void initChannel(Channel ch) throws Exception {
                    ch.pipeline().addLast(new SdkHandler(pkgManager,()->{onLogined();}));
                    ch.pipeline().addLast(new IdleStateHandler(0, 10, 0));
                    ch.pipeline().addLast(new PingPongHandler(uid, serverIp, serverPort));
                }
            };
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .option(ChannelOption.SO_SNDBUF, 2 * 1024 * 1024)//
                    .option(ChannelOption.SO_RCVBUF, 2 * 1024 * 1024)//
                    .handler(channelInitializer)
            ;
            Channel ch = b.bind(0).sync().channel();

            imLogin = new Login(ch, server);
            imChat = new Chat(ch, pkgManager, server);
            imLogin.login(uid);

            System.out.println("init");
            if (chatRun != null) {
                chatRun.run();
            }
            ch.closeFuture().await();
            //客户端等待15s用于接收服务端的应答消息，然后退出并释放资源
//            if (!ch.closeFuture().await(15000)) {
//                System.out.println("查询超时！");
//            }
            System.out.println("await");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }

    private void onLogined() {
        logined = true;
        if(imLoginCallback!=null){
            imLoginCallback.onLogined();
        }
    }

    public boolean isLogined() {
        return logined;
    }

    public void setImOnReceive(IMOnReceive imOnReceive) {
        this.imOnReceive = imOnReceive;
    }

    public void onReceive(PkgInfo pkgInfo) {
        if (imOnReceive != null) {
            imOnReceive.onReceive(pkgInfo);
        }
    }

    public void releaseListener() {
        imOnReceive = null;
    }
}
