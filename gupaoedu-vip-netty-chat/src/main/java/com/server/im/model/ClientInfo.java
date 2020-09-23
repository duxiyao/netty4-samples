package com.server.im.model;

import java.net.InetSocketAddress;
import java.util.Objects;

public class ClientInfo {
    private String userId;
    private InetSocketAddress inetSocketAddress;
    private String inetSocketAddressInfo;
    private long idelTime;

    public ClientInfo(String userId, InetSocketAddress inetSocketAddress, String inetSocketAddressInfo) {
        this.userId = userId;
        this.inetSocketAddress = inetSocketAddress;
        this.inetSocketAddressInfo = inetSocketAddressInfo;
        updateTime();
    }

    public String getUserId() {
        return userId;
    }

    public InetSocketAddress getInetSocketAddress() {
        return inetSocketAddress;
    }

    public String getInetSocketAddressInfo() {
        return inetSocketAddressInfo;
    }

    public void update(InetSocketAddress inetSocketAddress, String inetSocketAddressInfo) {
        updateTime();
        this.inetSocketAddress = inetSocketAddress;
        this.inetSocketAddressInfo = inetSocketAddressInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientInfo)) return false;
        ClientInfo that = (ClientInfo) o;
        return Objects.equals(getUserId(), that.getUserId()) &&
                Objects.equals(getInetSocketAddress(), that.getInetSocketAddress()) &&
                Objects.equals(getInetSocketAddressInfo(), that.getInetSocketAddressInfo());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUserId(), getInetSocketAddress(), getInetSocketAddressInfo());
    }

    public void updateTime() {

        idelTime = System.currentTimeMillis();
    }

    public long getIdelTime(){
        return idelTime;
    }
}
