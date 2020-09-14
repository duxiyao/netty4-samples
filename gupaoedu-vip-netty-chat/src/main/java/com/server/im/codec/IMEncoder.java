package com.server.im.codec;

import com.server.im.model.PkgInfo;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IMEncoder {
    public static ByteBuf encode(ChannelHandlerContext ctx, PkgInfo pkgInfo){
        String fromId=pkgInfo.getFrom();
        String toId=pkgInfo.getTo();
        byte type=pkgInfo.getType();
        byte version=pkgInfo.getVersion();
        String pkgId=pkgInfo.getPkgId();
        byte pkgcnt=pkgInfo.getPkgCnt();
        byte cpkgn=pkgInfo.getcPkgn();
        byte[] data=pkgInfo.getData();

        ByteBuf newBuf = ctx.alloc().buffer(1);
        byte[] from=fromId.getBytes();
        newBuf.writeByte(from.length);
        newBuf.writeBytes(from);
        byte[] to=toId.getBytes();
        newBuf.writeByte(to.length);
        newBuf.writeBytes(to);

        newBuf.writeByte(type);
        newBuf.writeByte(version);

        byte[] pkgid=pkgId.getBytes();
        newBuf.writeByte(pkgid.length);
        newBuf.writeBytes(pkgid);

        newBuf.writeByte(pkgcnt);
        newBuf.writeByte(cpkgn);

        newBuf.writeShort(data.length);
        newBuf.writeBytes(data);

        return newBuf;
    }
}
