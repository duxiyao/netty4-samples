package com.server.im.android.clinet.sdk.model;

import io.netty.buffer.ByteBuf;

import java.util.List;

public class WholePkg {
    private List<ByteBuf> byteBufs;
    private PkgInfo pkgInfo;
    private long initTime = System.currentTimeMillis();

    public WholePkg(List<ByteBuf> byteBufs, PkgInfo pkgInfo) {
        this.pkgInfo = pkgInfo;
        this.byteBufs = byteBufs;
    }

    public long getInitTime() {
        return initTime;
    }

    public String getPkgId() {
        return pkgInfo.getPkgId();
    }

    public ByteBuf get(int i) {
        try {
            ByteBuf byteBuf = byteBufs.get(i - 1);
            byteBuf.retain();
            return byteBuf;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

//    public void release() {
//        try {
//            byteBufs.parallelStream().forEach(o -> {
//                try {
//                    if (o.refCnt() > 0) {
//                        o.release();
//                        o = null;
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            });
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
