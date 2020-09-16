package com.server.im.codec;

import com.server.im.model.PkgInfo;
import com.server.im.udp.server.PkgManager;
import com.server.im.udp.server.StateManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
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
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
//           parse and add
        InetSocketAddress inetSocketAddress = msg.sender();

        ByteBuf byteBuf = msg.content();
        PkgInfo pkgInfo = decode(byteBuf, pkgManager);
        byteBuf.release();

        String userId = pkgInfo.getFrom();

        // TODO: 2020/9/11 判断是否是本relay服务维护，不是的话需要进行relay转发
        String to = pkgInfo.getTo();
//        if 不是本relay{
//            查询对应relay并进行relay转发 ，是否需要缓存对应relay？方便后续再有转发
//            return;
//        }

        if (!stateManager.contains(inetSocketAddress)) {
            //维护客户端通讯通道
            if (userId != null) {
                stateManager.add(userId, inetSocketAddress);
            }
            String req = "【服务器】您好 " + userId + "," + inetSocketAddress.toString();
            log.info(req);
            ctx.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(req, CharsetUtil.UTF_8), inetSocketAddress));
        }

        //判断类型，如果是客户端索取丢失的数据包
        // TODO: 2020/9/14 索取内存
        if (PkgInfo.TYPE_OBTAIN == pkgInfo.getType()) {
            //若内存有则回馈客户端；若没有则告诉客户端过期了
            // TODO: 2020/9/14

            pkgManager.remove(pkgInfo.getPkgId());
        } else {
            //转发
            InetSocketAddress toAddr = stateManager.get(to);
            if (toAddr != null) {
                transferTo(ctx, pkgInfo, toAddr);
            } else {
                log.info("无法发送给目标");
            }
        }

        ReferenceCountUtil.release(msg);
    }

    private void transferTo(ChannelHandlerContext ctx, PkgInfo pkgInfo, InetSocketAddress inetSocketAddress) {
        try {
            List<ByteBuf> bufs = IMEncoder.encode(ctx, pkgInfo);
            ByteBuf buf = bufs.get(0);
            ctx.writeAndFlush(new DatagramPacket(buf, inetSocketAddress));
            buf.release();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static PkgInfo decode(ByteBuf byteBuf, PkgManager msgManager) throws UnsupportedEncodingException {

        String fromId=byteBuf.readCharSequence(IMEncoder.ID_LEN, IMEncoder.CODESET).toString();
        String toId=byteBuf.readCharSequence(IMEncoder.ID_LEN, IMEncoder.CODESET).toString();
        String pkgId=byteBuf.readCharSequence(IMEncoder.ID_LEN, IMEncoder.CODESET).toString();

        byte type = byteBuf.readByte();
        byte version = byteBuf.readByte();
        byte pkgcnt = byteBuf.readByte();
        byte cpkgn = byteBuf.readByte();

        short datalen = byteBuf.readShort();
        byte[] data = new byte[datalen];
        byteBuf.readBytes(data);

        //解码
//        Message message = msgManager.get(pkgId);//客户端需要获取并保持，直到data数据获取完整后进行拼装
        PkgInfo pkgInfo = msgManager.get(pkgId);//客户端需要获取并保持，直到data数据获取完整后进行拼装。服务端只需缓存在内存里，当客户端需要时候给予即可。若是分布式，可考虑存放在redis中
        pkgInfo.setFrom(fromId);
        pkgInfo.setTo(toId);
        pkgInfo.setPkgId(pkgId);
        pkgInfo.setType(type);
        pkgInfo.setVersion(version);
        pkgInfo.setPkgCnt(pkgcnt);
        pkgInfo.setcPkgn(cpkgn);
        pkgInfo.addData(data);
        pkgInfo.setServerReceTime(System.currentTimeMillis());
        msgManager.checkPkg();
        return pkgInfo;
    }

}
