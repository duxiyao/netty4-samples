package com.server.im.codec;

import com.server.im.model.PkgInfo;

import java.util.UUID;

public class MessageUtil {

//    private static final String id=UUID.randomUUID().toString();
    public static final String id="4c69bacb-cd0c-4ea9-ab02-17ba6816f6c9";
    public static final String toid="4c69bacb-cd0c-4ea9-ab02-17ba6816f6c8";

    PkgInfo pkgInfo;
    private MessageUtil(){
        pkgInfo=new PkgInfo();
        pkgInfo.setVersion((byte) 1);
        pkgInfo.setPkgId(UUID.randomUUID().toString());
    }
    public static MessageUtil builder(){
        MessageUtil messageUtil=new MessageUtil();
        return messageUtil;
    }

    public MessageUtil setFrom(String from){
        pkgInfo.setFrom(from);
        return this;
    }

    public MessageUtil setTo(String to){
        pkgInfo.setTo(to);
        return this;
    }

    public MessageUtil setType(byte type){
        pkgInfo.setType(type);
        return this;
    }

    public MessageUtil setData(byte[] a){
        pkgInfo.addData(a);
        return this;
    }

    public MessageUtil addData(byte[] a){
        pkgInfo.addData(a);
        return this;
    }

    public PkgInfo build(){
        return pkgInfo;
    }

    public static PkgInfo buildBytes(String uid,String to,byte[] msg){
        MessageUtil messageUtil=builder();
        messageUtil.setFrom(uid);
        messageUtil.setTo(to);
        messageUtil.setType(PkgInfo.TYPE_TRANSFER_TXT);
        messageUtil.setData(msg);
        return messageUtil.build();
    }

    public static PkgInfo buildMsg(String to,String msg){
        return buildMsg(id,to,msg);
    }

    public static PkgInfo buildMsg(String uid,String to,String msg){
        MessageUtil messageUtil=builder();
        messageUtil.setFrom(uid);
        messageUtil.setTo(to);
        messageUtil.setType(PkgInfo.TYPE_TRANSFER_TXT);
        messageUtil.setData(msg.getBytes(IMEncoder.CODESET));
        return messageUtil.build();
    }

    public static PkgInfo buildLoginMsg(String msg){
        MessageUtil messageUtil=builder();
        messageUtil.setFrom(id);
        messageUtil.setTo("00000000-0000-0000-0000-000000000000");
        messageUtil.setType(PkgInfo.TYPE_LOGIN);
        messageUtil.setData(msg.getBytes(IMEncoder.CODESET));
        return messageUtil.build();
    }

    public static PkgInfo buildHeartBeatMsg(String msg){
        MessageUtil messageUtil=builder();
        messageUtil.setFrom(id);
        messageUtil.setTo("00000000-0000-0000-0000-000000000000");
        messageUtil.setType(PkgInfo.TYPE_HEART_BEAT);
        messageUtil.setData(msg.getBytes(IMEncoder.CODESET));
        return messageUtil.build();
    }
}
