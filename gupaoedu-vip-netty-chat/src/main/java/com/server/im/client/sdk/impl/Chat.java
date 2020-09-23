package com.server.im.client.sdk.impl;

import com.server.im.client.sdk.IMChat;

import com.server.im.client.sdk.IMSdk;
import com.server.im.codec.IMEncoder;
import com.server.im.codec.MessageUtil;
import com.server.im.model.PkgInfo;
import com.server.im.model.WaitForFinish;
import com.server.im.model.WholePkg;
import com.server.im.udp.server.PkgManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.socket.DatagramPacket;

import java.net.InetSocketAddress;
import java.util.List;

public class Chat implements IMChat {

    private Channel channel;
    private PkgManager pkgManager;
    private InetSocketAddress server;
    private String uid;

    public Chat(Channel channel, PkgManager pkgManager, InetSocketAddress server) {
        this.channel = channel;
        this.pkgManager=pkgManager;
        this.server=server;
        uid=IMSdk.getInstance().getUid();
    }

    @Override
    public void send(String toId, byte[] data) {
        if (channel == null)
            return;
        PkgInfo pkgInfo = MessageUtil.buildBytes(uid,toId, data);
        List<ByteBuf> datas = IMEncoder.encode(channel, pkgInfo);
        pkgManager.addWholePkg(new WholePkg(datas, pkgInfo));
        for (ByteBuf byteBuf : datas) {
            channel.write(new DatagramPacket(byteBuf, server));
        }
        pkgManager.addWaitForFinish(new WaitForFinish(server, channel.pipeline().lastContext(), pkgInfo));
        channel.flush();
    }

    @Override
    public void send(String toId, String msg) {
        if (channel == null)
            return;
        PkgInfo pkgInfo = MessageUtil.buildMsg(uid,toId, msg);
        List<ByteBuf> datas = IMEncoder.encode(channel, pkgInfo);
        pkgManager.addWholePkg(new WholePkg(datas, pkgInfo));
        for (ByteBuf byteBuf : datas) {
            channel.write(new DatagramPacket(byteBuf, server));
        }
        pkgManager.addWaitForFinish(new WaitForFinish(server, channel.pipeline().lastContext(), pkgInfo));
        channel.flush();
    }
}
