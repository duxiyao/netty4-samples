package com.server.im.android.clinet.sdk;

public interface IMChat {
    void send(String toId, byte[] data);
    void sendVideo(String toId, byte[] data);
    void sendAudio(String toId, byte[] data);
    void send(String toId, String msg);
    void sendTransparentTxt(String toId, String msg);
    void setMute(boolean flag);
    boolean isMute();
}
