package com.server.im.udp.server;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class StateManager {
    //userId InetSocketAddress
    private ConcurrentHashMap<String, InetSocketAddress> map = new ConcurrentHashMap<>();
    //ip信息
    private ConcurrentSkipListSet<String> set = new ConcurrentSkipListSet<>();

    public void add(String userId, InetSocketAddress inetSocketAddress) {
        set.add(inetSocketAddress.toString());
        map.put(userId, inetSocketAddress);
    }

    public InetSocketAddress get(String userId) {
        InetSocketAddress inetSocketAddress = map.get(userId);
        if (inetSocketAddress == null) {
            rm(userId);
        }
        return inetSocketAddress;
    }

    public void rm(String userId) {
        InetSocketAddress inetSocketAddress = map.get(userId);
        if(inetSocketAddress==null){
            return;
        }
        String ipinfo = inetSocketAddress.toString();
        set.remove(ipinfo);
        map.remove(userId);
    }

    public boolean contains(InetSocketAddress inetSocketAddress) {
        return set.contains(inetSocketAddress.toString());
    }
}
