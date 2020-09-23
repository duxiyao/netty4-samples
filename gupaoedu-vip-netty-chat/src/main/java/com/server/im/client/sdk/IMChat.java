package com.server.im.client.sdk;

public interface IMChat {
    void send(String toId,byte[] data);
    void send(String toId,String msg);
}
