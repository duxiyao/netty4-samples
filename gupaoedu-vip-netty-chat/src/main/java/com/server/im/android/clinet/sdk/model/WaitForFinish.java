package com.server.im.android.clinet.sdk.model;

import com.server.im.android.clinet.sdk.codec.IMEncoder;
import com.server.im.android.clinet.sdk.manager.PkgManager;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;

import java.net.InetSocketAddress;
import java.util.Objects;

public class WaitForFinish {
    private InetSocketAddress to;
    private ChannelHandlerContext ctx;
    private PkgInfo pkgInfo;
    private long initTime=System.currentTimeMillis();
    private int cnt=0;

    public WaitForFinish(InetSocketAddress to, ChannelHandlerContext ctx, PkgInfo info) {
        this.to = to;
        this.ctx = ctx;
        pkgInfo=new PkgInfo();
        pkgInfo.setVersion((byte) 1);
        pkgInfo.setTo(info.getTo());
        pkgInfo.setFrom(info.getFrom());
        pkgInfo.setPkgCnt((byte) 1);
        pkgInfo.setcPkgn((byte) 1);
        pkgInfo.setType(PkgInfo.TYPE_PKG_FINISH);
        pkgInfo.setPkgId(info.getPkgId());
    }

    public boolean send() {
        if(cnt>=2){
            return false;
        }
        cnt++;
        boolean flag = false;
        if (to == null || ctx == null || pkgInfo == null) {
            return flag;
        }
        if(System.currentTimeMillis()-initTime> PkgManager.MAX_ALIVE*1000){
            return flag;
        }
        try {
            ByteBuf buf = IMEncoder.encodeOne(ctx.channel(), pkgInfo);
            if (buf != null) {
                ctx.write(new DatagramPacket(buf, to));
            }
            ctx.flush();
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WaitForFinish)) return false;
        WaitForFinish that = (WaitForFinish) o;
        return Objects.equals(pkgInfo.getPkgId(), that.pkgInfo.getPkgId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(pkgInfo.getPkgId());
    }
}
