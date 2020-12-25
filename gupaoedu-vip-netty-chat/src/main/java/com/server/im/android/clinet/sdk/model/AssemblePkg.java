package com.server.im.android.clinet.sdk.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class AssemblePkg {
    private String id;
    //pkgid+cpkgn,单个pkginfo包
    private Map<String, PkgInfo> pkgInfos = new ConcurrentHashMap();
    private List<Byte> bytes = new CopyOnWriteArrayList<>();
    private byte total;
    private PkgInfo completed;
    private long serverReceTime;

    public AssemblePkg(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void addOne(PkgInfo pkgInfo) {
        String id = pkgInfo.getPkgId();
        if (!this.id.equals(id)) {
            return;
        }
        byte cpkgn = pkgInfo.getcPkgn();
        total = pkgInfo.getPkgCnt();

        if (!pkgInfos.containsKey(id + cpkgn)) {
            bytes.add(cpkgn);
            pkgInfos.put(id + cpkgn, pkgInfo);
        }
    }

    /**
     * 获取缺少的包
     *
     * @return
     */
    public List<Byte> getLackPkg() {
        List<Byte> tmp = new ArrayList<>();
        for (byte i = 1; i <= total; i++) {
            if (bytes.contains(i)) {
                continue;
            }
            tmp.add(i);
        }
        return tmp;
    }

    public PkgInfo get(byte n) {
        return pkgInfos.get(id + n);
    }

    public boolean quickJudgeAssemble(){
        if (completed != null) {
            return true;
        }
        return false;
    }

    /**
     * 组装包
     *
     * @return true组装成功
     */
    public boolean assemble() {
        if (completed != null) {
            return true;
        }
        List<Byte> tmp = getLackPkg();
        if (tmp.size() > 0) {
            return false;
        }

        bytes.sort(comparator);
        for (byte i : bytes) {
            String key = id + i;
            PkgInfo pkgInfo = pkgInfos.get(key);
            if (completed == null) {
                completed = new PkgInfo();
            }
            completed.setFrom(pkgInfo.getFrom());
            completed.setTo(pkgInfo.getTo());
            completed.setType(pkgInfo.getType());
            completed.setPkgId(pkgInfo.getPkgId());
            completed.setVersion(pkgInfo.getVersion());
            completed.setPkgCnt(pkgInfo.getPkgCnt());
            completed.setcPkgn(pkgInfo.getcPkgn());
            serverReceTime = System.currentTimeMillis();

            completed.addData(pkgInfo.getData());
        }
        return true;
    }

    private Comparator<Byte> comparator = new Comparator<Byte>() {
        @Override
        public int compare(Byte o1, Byte o2) {
            return o1 - o2;
        }
    };

    /**
     * 获取组装包
     *
     * @return
     */
    public PkgInfo getAssemblePkg() {
        return completed;
    }

    public Long getServerReceTime() {
        return serverReceTime;
    }
}
