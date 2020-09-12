package com.server.im.codec;

import com.server.im.model.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IMEncoder {
    public static ByteBuf encode(ChannelHandlerContext ctx,Message message){
        
        ByteBuf newBuf = ctx.alloc().buffer(1);
        newBuf.writeByte(1);
        // TODO: 2020/9/11  
        return newBuf;
    }
}
