package com.server.im.udp.server;

import com.server.im.model.ClientInfo;
import com.sun.imageio.plugins.common.ImageUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

@Slf4j
public class StateManager {
    //ip信息
    private ConcurrentSkipListSet<String> set = new ConcurrentSkipListSet<>();

    //userId ClientInfo
    private ConcurrentHashMap<String, ClientInfo> clientInfoConcurrentHashMap = new ConcurrentHashMap<>();

    public void add(String userId, InetSocketAddress inetSocketAddress) {
        set.add(inetSocketAddress.toString());
        clientInfoConcurrentHashMap.put(userId, new ClientInfo(userId, inetSocketAddress, inetSocketAddress.toString()));
    }

    public InetSocketAddress get(String userId) {
        ClientInfo clientInfo = null;
        try {
            clientInfo = clientInfoConcurrentHashMap.get(userId);
            return clientInfo.getInetSocketAddress();
        } catch (Exception e) {
            log.error("StateManager.get clientInfo==null " + (clientInfo == null));
        }
        return null;
    }

    public void rm(String userId) {
        ClientInfo clientInfo = clientInfoConcurrentHashMap.get(userId);
        if (clientInfo == null) {
            return;
        }
        String ipinfo = clientInfo.getInetSocketAddressInfo();
        set.remove(ipinfo);
        clientInfoConcurrentHashMap.remove(userId);
    }

    public boolean contains(InetSocketAddress inetSocketAddress) {
        return set.contains(inetSocketAddress.toString());
    }

    public void update(String userId, InetSocketAddress inetSocketAddress) {
        ClientInfo clientInfo = null;
        if (!clientInfoConcurrentHashMap.containsKey(userId)) {
            clientInfo = new ClientInfo(userId, inetSocketAddress, inetSocketAddress.toString());
            clientInfoConcurrentHashMap.put(userId, clientInfo);
            set.add(inetSocketAddress.toString());
        } else {
            clientInfo = clientInfoConcurrentHashMap.get(userId);
            if (!inetSocketAddress.equals(clientInfo.getInetSocketAddress())) {
                clientInfo.update(inetSocketAddress, inetSocketAddress.toString());
            }
        }
        clientInfo.updateTime();
    }

    public void checkTimeout(long cur) {
        try {
            Collection<ClientInfo> collection = clientInfoConcurrentHashMap.values();
            System.out.println(Thread.currentThread().getName() + "------checkTimeout------" + collection.size());
            for (ClientInfo clientInfo : collection) {
                try {
                    long target = clientInfo.getIdelTime();
                    if (cur - target > PkgManager.MAX_ALIVE * 1000) {
                        rm(clientInfo.getUserId());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
