package com.server.im.model;

import com.server.im.codec.IMEncoder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class PkgInfo {
    public final static byte TYPE_OBTAIN = 1;
    public final static byte TYPE_TRANSFER_TXT = 11;
    private String from;
    private String to;
    private Byte type;
    private Byte version;
    private String pkgId;
    private Byte pkgCnt;
    private Byte cPkgn;
    private Long serverReceTime;
    private ByteArrayOutputStream data = new ByteArrayOutputStream();


    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public Byte getType() {
        return type;
    }

    public void setType(Byte type) {
        this.type = type;
    }

    public Byte getVersion() {
        return version;
    }

    public void setVersion(Byte version) {
        this.version = version;
    }

    public String getPkgId() {
        return pkgId;
    }

    public void setPkgId(String pkgId) {
        this.pkgId = pkgId;
    }

    public Byte getPkgCnt() {
        return pkgCnt;
    }

    public void setPkgCnt(Byte pkgCnt) {
        this.pkgCnt = pkgCnt;
    }

    public Byte getcPkgn() {
        return cPkgn;
    }

    public void setcPkgn(Byte cPkgn) {
        this.cPkgn = cPkgn;
    }

    public void addData(byte[] d) {
        try {
            data.write(d);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] getData() {
        return data.toByteArray();
    }

    public Long getServerReceTime() {
        return serverReceTime;
    }

    public void setServerReceTime(Long serverReceTime) {
        this.serverReceTime = serverReceTime;
    }

    public ByteBuf getBase(ByteBuf byteBuffer,byte total, byte cur, short datalen) {
        if(byteBuffer==null) {
            byteBuffer = Unpooled.buffer(baseLength() + datalen);
        }
//        ByteBuffer byteBuffer = ByteBuffer.allocate(baseLength()+datalen);
        byteBuffer.writeBytes(from.getBytes(IMEncoder.CODESET));
        byteBuffer.writeBytes(to.getBytes(IMEncoder.CODESET));
        byteBuffer.writeBytes(pkgId.getBytes(IMEncoder.CODESET));

        byteBuffer.writeByte(type);
        byteBuffer.writeByte(version);

        byteBuffer.writeByte(total);
        byteBuffer.writeByte(cur);
        byteBuffer.writeShort(datalen);

        return byteBuffer;
    }

    public int baseLength() {
        return IMEncoder.ID_LEN * 3 + 6;
    }

    public int length() {
        try {
            return baseLength()
                    + data.size();
        } catch (Exception e) {
            return -1;
        }
    }

    @Override
    public String toString() {
        return "PkgInfo{" +
                "from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", type=" + type +
                ", version=" + version +
                ", pkgId='" + pkgId + '\'' +
                ", pkgCnt=" + pkgCnt +
                ", cPkgn=" + cPkgn +
                ", serverReceTime=" + serverReceTime +
                ", data=" + data +
                '}';
    }
}
