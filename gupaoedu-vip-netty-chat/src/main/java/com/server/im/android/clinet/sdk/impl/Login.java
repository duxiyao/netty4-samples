package com.server.im.android.clinet.sdk.impl;

import com.server.im.android.clinet.sdk.IMLogin;
import com.server.im.android.clinet.sdk.codec.IMEncoder;
import com.server.im.android.clinet.sdk.codec.MessageUtil;
import com.server.im.android.clinet.sdk.model.PkgInfo;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.socket.DatagramPacket;

import java.net.InetSocketAddress;
import java.util.List;


public class Login implements IMLogin {
    private Channel channel;
    private InetSocketAddress server;

    public Login(Channel channel, InetSocketAddress server) {
        this.channel = channel;
        this.server=server;
    }

    @Override
    public void login(String uid) {
        if(channel==null)
            return;
        PkgInfo pkgInfo = MessageUtil.buildLoginMsg("");
        pkgInfo.setFrom(uid);
        List<ByteBuf> datas = IMEncoder.encode(channel, pkgInfo);
        for (ByteBuf byteBuf : datas) {
            channel.write(new DatagramPacket(byteBuf, server));
            break;
        }
        channel.flush();
    }

    @Override
    public void logout() {
        if(channel==null)
            return;
    }

}
