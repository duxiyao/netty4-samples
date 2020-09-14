package com.server.im.udp.server;

import com.server.im.model.PkgInfo;

import java.util.Map;
import java.util.concurrent.*;

public class PkgManager {

    private final static int MAX_ALIVE = 30;//缓存30秒
    private Map<String, PkgInfo> pkgInfos = new ConcurrentHashMap();
    private ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
            1, 1, 0,
            TimeUnit.SECONDS, new LinkedBlockingDeque<>(10),
            new ThreadPoolExecutor.DiscardPolicy()
    );

    public void checkPkg() {
        threadPoolExecutor.execute(() -> {
            for (PkgInfo pkgInfo : pkgInfos.values()) {
                try {
                    long cur = System.currentTimeMillis();
                    long m = pkgInfo.getServerReceTime();
                    if (cur - m > MAX_ALIVE * 1000) {
                        pkgInfos.remove(pkgInfo.getPkgId());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void remove(String id){
        pkgInfos.remove(id);
    }

    public synchronized PkgInfo get(String id) {

        PkgInfo pkgInfo = pkgInfos.get(id);
        if (pkgInfo == null ) {
            pkgInfo = new PkgInfo();
            pkgInfos.put(id, pkgInfo);
        }
        return pkgInfo;
    }
}
