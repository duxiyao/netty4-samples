package com.server.im.codec;

import com.server.im.model.PkgInfo;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class IMEncoder {

    public static final int ID_LEN = 36;
    public static final String CODE = "utf-8";
    public static final Charset CODESET = CharsetUtil.UTF_8;
    public static final int MAX_LEN = 1472;

    /**
     * 将pkgInfo所有数据，按照MAX_LEN进行编码分包
     *
     * @param ctx
     * @param pkgInfo
     * @return
     */
    public static List<ByteBuf> encode(Channel ctx, PkgInfo pkgInfo) {
        List<ByteBuf> ret = new ArrayList<>();
        int len = pkgInfo.length();
        int baselen = pkgInfo.baseLength();

        int datalen = len - baselen;
        int max = MAX_LEN - baselen;

        int remain = datalen;
        int n = 1;
        while (remain > max) {
            n++;
            remain = remain - max;
        }

        byte[] data = pkgInfo.getData();
        ByteBuf bdata = null;
        if (ctx != null) {
            bdata = ctx.alloc().buffer(data.length);
            bdata.writeBytes(data);
        } else {
            bdata = Unpooled.wrappedBuffer(data);
        }
//        ByteBuffer bdata = ByteBuffer.wrap(data);
        for (int i = 0; i < n; i++) {
            int cur = i + 1;
            int dlen = max;
            if (i == n - 1) {
                dlen = bdata.readableBytes();
            }
            ByteBuf byteBuffer = null;
            if (ctx != null) {
                byteBuffer = ctx.alloc().buffer(pkgInfo.baseLength() + datalen);
            }
            ByteBuf buffer = pkgInfo.getBase(byteBuffer, (byte) n, (byte) cur, (short) dlen);
            byte[] tmp = new byte[dlen];
            bdata.readBytes(tmp);
            buffer.writeBytes(tmp);
            ret.add(buffer);
        }
        bdata.release();
        return ret;
    }

    /**
     * 编码一个包
     *
     * @param ctx
     * @param pkgInfo
     * @return
     */
    public static ByteBuf encodeOne(Channel ctx, PkgInfo pkgInfo) {
        byte[] data = pkgInfo.getData();
        ByteBuf byteBuffer = ctx.alloc().buffer(pkgInfo.baseLength() + data.length);
        Byte total = pkgInfo.getPkgCnt();
        Byte cur = pkgInfo.getcPkgn();
        if (cur == null) {
            cur = 1;
        }
        if (total == null) {
            total = 1;
            cur = 1;
        }
        ByteBuf buffer = pkgInfo.getBase(byteBuffer, total, cur, (short) data.length);
        buffer.writeBytes(data);
        return buffer;
    }
}
