package com.server.im.model;

import com.server.im.codec.IMEncoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;

import java.net.InetSocketAddress;
import java.util.Objects;

public class WaitForFinish {
    private InetSocketAddress to;
    private ChannelHandlerContext ctx;
    private PkgInfo pkgInfo;

    public WaitForFinish(InetSocketAddress to, ChannelHandlerContext ctx, PkgInfo pkgInfo) {
        this.to = to;
        this.ctx = ctx;
        this.pkgInfo = pkgInfo;
    }

    public boolean send() {
        boolean flag = false;
        if (to == null || ctx == null || pkgInfo == null) {
            return flag;
        }
        pkgInfo.setType(PkgInfo.TYPE_PKG_FINISH);
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
