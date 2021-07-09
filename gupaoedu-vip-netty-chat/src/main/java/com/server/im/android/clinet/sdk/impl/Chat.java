package com.server.im.android.clinet.sdk.impl;

import com.server.im.android.clinet.sdk.IMChat;
import com.server.im.android.clinet.sdk.IMSdk;
import com.server.im.android.clinet.sdk.codec.IMEncoder;
import com.server.im.android.clinet.sdk.codec.MessageUtil;
import com.server.im.android.clinet.sdk.manager.PkgManager;
import com.server.im.android.clinet.sdk.model.PkgInfo;
import com.server.im.android.clinet.sdk.model.WaitForFinish;
import com.server.im.android.clinet.sdk.model.WholePkg;

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
    private volatile boolean isMute = false;

    public Chat(Channel channel, PkgManager pkgManager, InetSocketAddress server) {
        this.channel = channel;
        this.pkgManager = pkgManager;
        this.server = server;
        uid = IMSdk.getInstance().getUid();
    }

    @Override
    public void send(String toId, byte[] data) {
        if (channel == null)
            return;
        PkgInfo pkgInfo = MessageUtil.buildBytes(uid, toId, data);
        List<ByteBuf> datas = IMEncoder.encode(channel, pkgInfo);
        pkgManager.addWholePkg(new WholePkg(datas, pkgInfo));
        for (ByteBuf byteBuf : datas) {
            channel.write(new DatagramPacket(byteBuf, server));
        }
        pkgManager.addWaitForFinish(new WaitForFinish(server, channel.pipeline().lastContext(), pkgInfo));
        channel.flush();
    }

    @Override
    public void sendVideo(String toId, byte[] data) {
        if (channel == null)
            return;
        PkgInfo pkgInfo = MessageUtil.buildBytes(uid, toId, data);
        pkgInfo.setType(PkgInfo.TYPE_TRANSFER_VIDEO);
        List<ByteBuf> datas = IMEncoder.encode(channel, pkgInfo);
        pkgManager.addWholePkg(new WholePkg(datas, pkgInfo));
        for (ByteBuf byteBuf : datas) {
            channel.write(new DatagramPacket(byteBuf, server));
        }
        pkgManager.addWaitForFinish(new WaitForFinish(server, channel.pipeline().lastContext(), pkgInfo));
        channel.flush();
    }

    @Override
    public void sendAudio(String toId, byte[] data) {
        if (channel == null || isMute)
            return;
        PkgInfo pkgInfo = MessageUtil.buildBytes(uid, toId, data);
        pkgInfo.setType(PkgInfo.TYPE_TRANSFER_AUDIO);
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
        PkgInfo pkgInfo = MessageUtil.buildMsg(uid, toId, msg);
        List<ByteBuf> datas = IMEncoder.encode(channel, pkgInfo);
        pkgManager.addWholePkg(new WholePkg(datas, pkgInfo));
        for (ByteBuf byteBuf : datas) {
            channel.write(new DatagramPacket(byteBuf, server));
        }
        pkgManager.addWaitForFinish(new WaitForFinish(server, channel.pipeline().lastContext(), pkgInfo));
        channel.flush();
    }

    @Override
    public void sendTransparentTxt(String toId, String msg) {
        if (channel == null)
            return;
        PkgInfo pkgInfo = MessageUtil.buildMsg(uid, toId, msg);
        pkgInfo.setType(PkgInfo.TYPE_TRANSFER_TRANSPARENT_TXT);
        List<ByteBuf> datas = IMEncoder.encode(channel, pkgInfo);
        pkgManager.addWholePkg(new WholePkg(datas, pkgInfo));
        for (ByteBuf byteBuf : datas) {
            channel.write(new DatagramPacket(byteBuf, server));
        }
        pkgManager.addWaitForFinish(new WaitForFinish(server, channel.pipeline().lastContext(), pkgInfo));
        channel.flush();
    }

    @Override
    public void setMute(boolean flag) {
        isMute = flag;
    }

    @Override
    public boolean isMute() {
        return isMute;
    }
}
