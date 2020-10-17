package com.server.im.model;

import com.server.im.codec.IMEncoder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;

public class PkgInfo {
    // TODO: 2020-10-16 增加告知客户端登录成功，或在线状态

    public final static byte TYPE_LOGIN = 0;
    public final static byte TYPE_OBTAIN = 1;
    /**
     * 每当一系列包发送完后，会发送发送完成标志，然后对方需要进行响应
     */
    public final static byte TYPE_PKG_FINISH = 2;
    /**
     * 当接收端收到TYPE_PKG_FINISH，检测该系列包是否完整，不完整则进行索取TYPE_OBTAIN，完整则回应TYPE_PKG_RECEIVE_FINISH
     */
    public final static byte TYPE_PKG_RECEIVE_FINISH = 3;
    public final static byte TYPE_PKG_REMOVED = 4;
    public final static byte TYPE_HEART_BEAT = 5;
    public final static byte TYPE_TRANSFER_TXT = 11;
    public final static byte TYPE_TRANSFER_TRANSPARENT_TXT = 12;//透传
    public final static byte TYPE_TRANSFER_VIDEO = 13;//
    public final static byte TYPE_TRANSFER_AUDIO = 14;//
    /**
     * 对方不在线
     */
    public final static byte TYPE_TARGET_OFFLINE = 50;
    private String from;
    private String to;
    private Byte type;
    private Byte version;
    private String pkgId;
    private Byte pkgCnt;
    private Byte cPkgn;
    private Long serverReceTime;
    private byte[] bdata;
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
        saveData();
        return bdata;
    }

    private void saveData() {
        if (bdata == null) {
            bdata = data.toByteArray();
        }
    }

    public Long getServerReceTime() {
        return serverReceTime;
    }

    public void setServerReceTime(Long serverReceTime) {
        this.serverReceTime = serverReceTime;
    }

    public ByteBuf getBase(ByteBuf byteBuffer, byte total, byte cur, short datalen) {
        if (byteBuffer == null) {
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
        saveData();
        return "PkgInfo{" +
                "from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", type=" + type +
                ", version=" + version +
                ", pkgId='" + pkgId + '\'' +
                ", pkgCnt=" + pkgCnt +
                ", cPkgn=" + cPkgn +
                ", serverReceTime=" + serverReceTime +
                ", data=" + bdata +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PkgInfo)) return false;
        PkgInfo pkgInfo = (PkgInfo) o;
//        return Objects.equals(getFrom(), pkgInfo.getFrom()) &&
//                Objects.equals(getTo(), pkgInfo.getTo()) &&
//                Objects.equals(getType(), pkgInfo.getType()) &&
//                Objects.equals(getVersion(), pkgInfo.getVersion()) &&
//                Objects.equals(getPkgId(), pkgInfo.getPkgId()) &&
//                Objects.equals(getPkgCnt(), pkgInfo.getPkgCnt()) &&
//                Objects.equals(getcPkgn(), pkgInfo.getcPkgn());
        return
                Objects.equals(getPkgId(), pkgInfo.getPkgId()) &&
                        Objects.equals(getcPkgn(), pkgInfo.getcPkgn());
    }

    @Override
    public int hashCode() {
//        return Objects.hash(getFrom(), getTo(), getType(), getVersion(), getPkgId(), getPkgCnt(), getcPkgn());
        return Objects.hash(getPkgId(), getcPkgn());
    }
}
