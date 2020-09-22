package com.server.im.codec;

import com.server.im.model.PkgInfo;
import com.server.im.model.WaitForFinish;
import com.server.im.udp.server.PkgManager;
import com.server.im.udp.server.StateManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class IMDecoder extends SimpleChannelInboundHandler<DatagramPacket> {
    private StateManager stateManager;
    private PkgManager pkgManager;

    public IMDecoder(StateManager stateManager, PkgManager pkgManager) {
        this.stateManager = stateManager;
        this.pkgManager = pkgManager;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
//           parse and add
        InetSocketAddress inetSocketAddress = msg.sender();

        ByteBuf byteBuf = msg.content();
        PkgInfo pkgInfo = PkgManager.decodeOne(byteBuf, pkgManager);

        String userId = pkgInfo.getFrom();
        log.info("receive data of " + userId + " pkgId=" + pkgInfo.getPkgId() + " type=" + pkgInfo.getType());

        // TODO: 2020/9/11 判断是否是本relay服务维护，不是的话需要进行relay转发
        String to = pkgInfo.getTo();
//        if 不是本relay{
//            查询对应relay并进行relay转发 ，是否需要缓存对应relay？方便后续再有转发
//            return;
//        }

        switch (pkgInfo.getType()) {
            case PkgInfo.TYPE_LOGIN:
                if (!stateManager.contains(inetSocketAddress)) {
                    //维护客户端通讯通道
                    if (userId != null) {
                        stateManager.add(userId, inetSocketAddress);
                    }
                    String req = "【服务器】您好 " + userId + "," + inetSocketAddress.toString();
                    log.info(req);
                }
                break;
            case PkgInfo.TYPE_OBTAIN:
                //判断类型，如果是客户端索取丢失的数据包
                // : 2020/9/14 索取内存
                //若内存有则回馈客户端；若没有则告诉客户端过期了
                // : 2020/9/14
                byte[] pkgns = pkgInfo.getData();
                for (byte n : pkgns) {
                    PkgInfo which = pkgManager.get(pkgInfo.getPkgId(), n);
                    if (which == null) {
                        writeRemoved(ctx, pkgInfo, inetSocketAddress);
                        break;
                    } else {
                        writeMissing(ctx, which, inetSocketAddress);
                    }
                }
                break;
            case PkgInfo.TYPE_PKG_RECEIVE_FINISH:
                pkgManager.removeWaitForFinish(new WaitForFinish(null, ctx, pkgInfo));
                pkgManager.remove(pkgInfo.getPkgId());
                break;
            case PkgInfo.TYPE_PKG_REMOVED:
                pkgManager.remove(pkgInfo.getPkgId());
                break;
            case PkgInfo.TYPE_PKG_FINISH:
//                if (pkgManager.get(pkgInfo.getPkgId()) == null) {
                    if (pkgManager.assemblePkg(pkgInfo.getPkgId())) {
                        log.info(pkgInfo.getPkgId() + " 组装成功");
                        // : 2020/9/21  组装成功 回应
                        responsePkgAssembled(ctx, pkgInfo, inetSocketAddress);
                        //转发
                        InetSocketAddress toAddr = stateManager.get(to);
                        if (toAddr != null) {
                            WaitForFinish waitForFinish = new WaitForFinish(toAddr, ctx, pkgInfo);
                            // : 2020/9/21 需要一直转发PkgInfo.TYPE_PKG_FINISH直到收到 PkgInfo.TYPE_PKG_RECEIVE_FINISH
                            pkgManager.addWaitForFinish(waitForFinish);
                        }
                    } else {
                        log.info(pkgInfo.getPkgId() + " 组装失败");
                        //组装失败
                        List<Byte> pkgn = pkgManager.getLackPkg(pkgInfo.getPkgId());
                        if (pkgn != null) {
                            // : 2020/9/21 像提供者索取
                            responsePkgObtain(ctx, pkgInfo, inetSocketAddress, pkgn);
                        } else {
                            byte total = pkgInfo.getPkgCnt();
                            List<Byte> ds = new ArrayList<>();
                            for (byte i = 1; i <= total; i++) {
                                ds.add(i);
                            }
                            responsePkgObtain(ctx, pkgInfo, inetSocketAddress, ds);
                        }
                    }
//                }
//                else {
//                    log.info(pkgInfo.getPkgId() + " 已组过包");
//                    // : 2020/9/21  组装成功 回应
//                    responsePkgAssembled(ctx, pkgInfo, inetSocketAddress);
//                }
                break;
            case PkgInfo.TYPE_TRANSFER_TXT:

                //转发
                InetSocketAddress toAddr = stateManager.get(to);
                if (toAddr != null) {
                    String req = "【服务器】转发 " + toAddr.toString();
                    log.info(req);
                    transferTo(ctx, pkgInfo, toAddr);
                    pkgManager.addOne(pkgInfo);
                } else {
                    log.info("无法发送给目标");
                }
                break;
            default:
                break;
        }

//        ReferenceCountUtil.release(msg);
    }

    private void writeRemoved(ChannelHandlerContext ctx, PkgInfo pkgInfo, InetSocketAddress inetSocketAddress) {
        pkgInfo.setType(PkgInfo.TYPE_PKG_REMOVED);
        transferTo(ctx, pkgInfo, inetSocketAddress);
    }

    private void writeMissing(ChannelHandlerContext ctx, PkgInfo which, InetSocketAddress inetSocketAddress) {
        transferTo(ctx, which, inetSocketAddress);
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
        log.info(pkgInfo.getPkgId() + " 回应："+inetSocketAddress.toString());
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
