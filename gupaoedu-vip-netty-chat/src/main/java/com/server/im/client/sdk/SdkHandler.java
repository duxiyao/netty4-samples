package com.server.im.client.sdk;

import com.server.im.codec.IMEncoder;
import com.server.im.model.PkgInfo;
import com.server.im.model.WaitForFinish;
import com.server.im.udp.server.PkgManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class SdkHandler extends
        SimpleChannelInboundHandler<DatagramPacket> {

    PkgManager pkgManager;

    public SdkHandler(PkgManager pkgManager) {
        this.pkgManager = pkgManager;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable
            cause) throws Exception {
        cause.printStackTrace();
//        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        InetSocketAddress inetSocketAddress = msg.sender();
        ByteBuf byteBuf = msg.content();
        PkgInfo pkgInfo = PkgManager.decodeOne(byteBuf, pkgManager);
        log.info("receive data of " + pkgInfo.getFrom() + " type=" + pkgInfo.getType());

        switch (pkgInfo.getType()) {
            case PkgInfo.TYPE_LOGIN:
                log.info("登录成功");
                break;
            case PkgInfo.TYPE_OBTAIN:
                byte[] pkgns = pkgInfo.getData();
                for (byte n : pkgns) {
                    ByteBuf which = pkgManager.getByteBuf(pkgInfo.getPkgId(), n);
                    if (which == null) {
                        pkgManager.removeWaitForFinish(new WaitForFinish(null, ctx, pkgInfo));
                        writeRemoved(ctx, pkgInfo, inetSocketAddress);
                        break;
                    } else {
                        log.info("----writeMissing");
                        writeMissing(ctx, which, inetSocketAddress);
                    }
                }
                break;
            case PkgInfo.TYPE_PKG_RECEIVE_FINISH:
                pkgManager.removeWaitForFinish(new WaitForFinish(null, ctx, pkgInfo));
                pkgManager.remove(pkgInfo.getPkgId());
                pkgManager.removeWholePkg(pkgInfo.getPkgId());
                break;
            case PkgInfo.TYPE_PKG_REMOVED:
            case PkgInfo.TYPE_TARGET_OFFLINE:
                pkgManager.removeWaitForFinish(new WaitForFinish(null, ctx, pkgInfo));
                pkgManager.removeWholePkg(pkgInfo.getPkgId());
                pkgManager.remove(pkgInfo.getPkgId());
                break;
            case PkgInfo.TYPE_PKG_FINISH:
                String pkgid = pkgInfo.getPkgId();
                if (pkgManager.get(pkgid) == null) {
                    if (pkgManager.assemblePkg(pkgid)) {
                        // : 2020/9/21  组装成功 回应
                        responsePkgAssembled(ctx, pkgInfo, inetSocketAddress);
                        PkgInfo data = pkgManager.get(pkgInfo.getPkgId());
                        System.out.println("client---" + data.toString());
                        IMSdk.getInstance().onReceive(data);
                    } else {
                        //组装失败
                        List<Byte> pkgn = pkgManager.getLackPkg(pkgInfo.getPkgId());
                        if (pkgn != null) {
                            // : 2020/9/21 像提供者索取
                            responsePkgObtain(ctx, pkgInfo, inetSocketAddress, pkgn);
                        } else {
                            byte total = pkgInfo.getPkgCnt();
                            List<Byte> ds = new ArrayList<>();
                            for (byte i = 0; i < total; i++) {
                                ds.add(i);
                            }
                            responsePkgObtain(ctx, pkgInfo, inetSocketAddress, ds);
                        }
                    }
                } else {
                    // : 2020/9/21  组装成功 回应
                    responsePkgAssembled(ctx, pkgInfo, inetSocketAddress);
                }
                break;
            case PkgInfo.TYPE_TRANSFER_TXT:
                pkgManager.addOne(pkgInfo);
                break;
            default:
                break;
        }
    }

    private void writeRemoved(ChannelHandlerContext ctx, PkgInfo pkgInfo, InetSocketAddress inetSocketAddress) {
        pkgInfo.setType(PkgInfo.TYPE_PKG_REMOVED);
        transferTo(ctx, pkgInfo, inetSocketAddress);
    }

    private void writeMissing(ChannelHandlerContext ctx, ByteBuf which, InetSocketAddress inetSocketAddress) {
        try {
            ByteBuf buf = which;
            if (buf != null) {
                ctx.write(new DatagramPacket(buf, inetSocketAddress));
            }
            ctx.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void responsePkgObtain(ChannelHandlerContext ctx, PkgInfo pkgInfo, InetSocketAddress inetSocketAddress, List<Byte> pkgn) {
        pkgInfo.setType(PkgInfo.TYPE_OBTAIN);
        byte[] d = new byte[pkgn.size()];
        for (int i = 0; i < pkgn.size(); i++) {
            d[i] = pkgn.get(i);
        }
        pkgInfo.addData(d);
        transferTo(ctx, pkgInfo, inetSocketAddress);
    }

    private void responsePkgAssembled(ChannelHandlerContext ctx, PkgInfo pkgInfo, InetSocketAddress inetSocketAddress) {
        pkgInfo.setType(PkgInfo.TYPE_PKG_RECEIVE_FINISH);
        transferTo(ctx, pkgInfo, inetSocketAddress);
    }

    private void transferTo(ChannelHandlerContext ctx, PkgInfo pkgInfo, InetSocketAddress inetSocketAddress) {
        try {
            ByteBuf buf = IMEncoder.encodeOne(ctx.channel(), pkgInfo);
            if (buf != null) {
                ctx.write(new DatagramPacket(buf, inetSocketAddress));
            }
            ctx.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}