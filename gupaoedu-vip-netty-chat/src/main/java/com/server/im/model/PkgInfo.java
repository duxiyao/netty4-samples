package com.server.im.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PkgInfo {
    public final static byte TYPE_OBTAIN = 1;
    public final static byte TYPE_TRANSFER_TXT = 11;
    private String to;
    private String from;
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
}
