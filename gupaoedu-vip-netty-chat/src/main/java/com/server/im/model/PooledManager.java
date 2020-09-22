package com.server.im.model;

import io.netty.buffer.PooledByteBufAllocator;
@Deprecated
public class PooledManager {

    private static class Inner {
        private static PooledManager instance = new PooledManager();
    }

    private PooledManager() {
    }

    public static PooledManager getInstance() {
        return Inner.instance;
    }

    public void test(){
//        PooledByteBufAllocator.DEFAULT.buffer()
    }
}
