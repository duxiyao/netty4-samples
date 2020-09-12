package com.server.im.codec;

import com.gupaoedu.vip.netty.chat.protocol.IMP;
import com.server.im.model.Message;
import com.server.im.udp.server.StateManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.msgpack.MessageTypeException;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.util.List;

@Slf4j
public class IMDecoder extends SimpleChannelInboundHandler<DatagramPacket> {
    public final static String CODE="utf-8";
    private StateManager stateManager;

    public IMDecoder(StateManager stateManager) {
        this.stateManager = stateManager;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
//           parse and add
        InetSocketAddress inetSocketAddress = msg.sender();

        ByteBuf byteBuf = msg.content();
        Message message = decode(byteBuf);
        byteBuf.release();

        String userId = message.getFrom();

        // TODO: 2020/9/11 判断是否是本服务维护，不是的话需要进行relay转发
        String to = message.getTo();
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

        //转发
        InetSocketAddress toAddr = stateManager.get(to);
        if (toAddr != null) {
            transferTo(ctx, message, toAddr);
        } else {
            log.info("无法发送给目标");
        }
    }

    private void transferTo(ChannelHandlerContext ctx, Message message, InetSocketAddress inetSocketAddress) {
        ByteBuf buf = IMEncoder.encode(ctx, message);
        ctx.writeAndFlush(new DatagramPacket(buf, inetSocketAddress));
        buf.release();
    }


    public static Message decode(ByteBuf byteBuf) throws UnsupportedEncodingException {
        byteBuf.clear();
        short idlen=byteBuf.readShort();
        byte[] pkgid=new byte[idlen];
        String pkgId=new String(pkgid,CODE);

        short pkgcount=byteBuf.readShort();
        short cpkgn=byteBuf.readShort();

        byte version = byteBuf.readByte();
        short pkgLen = byteBuf.readShort();
        byte type = byteBuf.readByte();
        short jsonLen = byteBuf.readShort();
        byte[] jsonb = new byte[jsonLen];
        byteBuf.readBytes(jsonb);
        String json=new String(jsonb,CODE);
        //剩余的data长度
        int rlen = pkgLen - 2 - 1 - 2 - jsonLen;
        byte[] remain = new byte[rlen];
        if (rlen > 0) {
            byteBuf.readBytes(remain);
        }

        Message message = new Message();
// TODO: 2020/9/11 解码
        message.setFrom("1");
        message.setTo("2");

        return message;
    }

//    @Override
//    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
//        try{
//            //先获取可读字节数
//            final int length = in.readableBytes();
//            final byte[] array = new byte[length];
//            String content = new String(array,in.readerIndex(),length);
//
//            //空消息不解析
//            if(!(null == content || "".equals(content.trim()))){
//                if(!IMP.isIMP(content)){
//                    ctx.channel().pipeline().remove(this);
//                    return;
//                }
//            }
//
//            in.getBytes(in.readerIndex(), array, 0, length);
//            out.add(array);
//            in.clear();
//        }catch(MessageTypeException e){
//            ctx.channel().pipeline().remove(this);
//        }
//    }

}
