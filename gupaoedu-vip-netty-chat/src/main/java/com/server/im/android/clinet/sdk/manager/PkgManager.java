package com.server.im.android.clinet.sdk.manager;

import com.server.im.android.clinet.sdk.codec.IMEncoder;
import com.server.im.android.clinet.sdk.model.AssemblePkg;
import com.server.im.android.clinet.sdk.model.PkgInfo;
import com.server.im.android.clinet.sdk.model.WaitForFinish;
import com.server.im.android.clinet.sdk.model.WholePkg;

import io.netty.buffer.ByteBuf;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class PkgManager {

    public final static int MAX_ALIVE = 10;//缓存30秒
    //pkgid+cpkgn,单个pkginfo包
    private Map<String, AssemblePkg> pkgMap = new ConcurrentHashMap();
    private CopyOnWriteArrayList<WaitForFinish> waitfininsh = new CopyOnWriteArrayList<>();
    private ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 1, 0l,
            TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(10),
            new ThreadPoolExecutor.DiscardOldestPolicy());

    //            new ThreadPoolExecutor(1, 1, 0l,
//            TimeUnit.SECONDS, new LinkedBlockingDeque<>(10),new ThreadPoolExecutor.DiscardOldestPolicy());
    private volatile boolean check = true;
    private Consumer<WaitForFinish> pkgInfoConsumer;
    private Map<String, WholePkg> wholePkgMap = new ConcurrentHashMap();

    public PkgManager() {
        checkPkg();
    }

    private void checkPkg() {
        threadPoolExecutor.execute(
                new Runnable() {
                    @Override
                    public void run() {

                        long checkPkgTime = System.currentTimeMillis();
                        while (check) {

                            try {

                                if (pkgInfoConsumer != null) {
                                    if (waitfininsh.size() > 0) {
//                                        System.out.println(Thread.currentThread().getName() + "------foreach------" + waitfininsh.size());
                                        waitfininsh.forEach(pkgInfoConsumer);
                                    }
                                }

                                long cur = System.currentTimeMillis();
                                if (cur - checkPkgTime > 5000) {

                                    Collection<AssemblePkg> collection = pkgMap.values();
                                    for (AssemblePkg assemblePkg : collection) {
                                        try {
                                            long m = assemblePkg.getServerReceTime();
                                            if (cur - m > MAX_ALIVE * 1000) {
                                                pkgMap.remove(assemblePkg.getId());
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    Collection<WholePkg> wholePkgs = wholePkgMap.values();
                                    for (WholePkg wholePkg : wholePkgs) {
                                        try {
                                            long m = wholePkg.getInitTime();
                                            if (cur - m > MAX_ALIVE * 1000) {
                                                removeWholePkg(wholePkg.getPkgId());
//                                                wholePkgMap.remove(wholePkg.getPkgId());
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    checkPkgTime = System.currentTimeMillis();
                                }

                                checkWholePkgCnt();
                                TimeUnit.MILLISECONDS.sleep(100);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
        );
    }

    public synchronized void addOne(PkgInfo pkgInfo) {
        String id = pkgInfo.getPkgId();
        AssemblePkg assemblePkg = pkgMap.get(id);
        if (assemblePkg == null) {
            assemblePkg = new AssemblePkg(id);
            pkgMap.put(id, assemblePkg);
        }
        assemblePkg.addOne(pkgInfo);
    }

    /**
     * 组装数据包
     *
     * @param id
     * @return
     */
    public synchronized boolean quickJudgeAssemblePkg(String id) {
        AssemblePkg assemblePkg = pkgMap.get(id);
        if (assemblePkg == null) {
            return false;
        }
        return assemblePkg.quickJudgeAssemble();
    }

    public synchronized boolean assemblePkg(String id) {
        AssemblePkg assemblePkg = pkgMap.get(id);
        if (assemblePkg == null) {
            return false;
        }
        return assemblePkg.assemble();
    }

    /**
     * 获取缺少的包
     *
     * @param id
     * @return
     */
    public List<Byte> getLackPkg(String id) {

        AssemblePkg assemblePkg = pkgMap.get(id);
        if (assemblePkg == null) {
            return null;
        }
        return assemblePkg.getLackPkg();
    }

    /**
     * @param id pkgid
     */
    public void remove(String id) {
        pkgMap.remove(id);
    }

    public PkgInfo get(String id) {
        AssemblePkg assemblePkg = pkgMap.get(id);
        if (assemblePkg != null)
            return assemblePkg.getAssemblePkg();
        return null;
    }

    public PkgInfo get(String id, byte n) {
        AssemblePkg assemblePkg = pkgMap.get(id);
        if (assemblePkg != null)
            return assemblePkg.get(n);
        return null;
    }

//    public static PkgInfo decode(ByteBuf byteBuf, PkgManager pkgManager) throws UnsupportedEncodingException {
//
//        String fromId = byteBuf.readCharSequence(IMEncoder.ID_LEN, IMEncoder.CODESET).toString();
//        String toId = byteBuf.readCharSequence(IMEncoder.ID_LEN, IMEncoder.CODESET).toString();
//        String pkgId = byteBuf.readCharSequence(IMEncoder.ID_LEN, IMEncoder.CODESET).toString();
//
//        byte type = byteBuf.readByte();
//        byte version = byteBuf.readByte();
//        byte pkgcnt = byteBuf.readByte();
//        byte cpkgn = byteBuf.readByte();
//
//        short datalen = byteBuf.readShort();
//        byte[] data = new byte[datalen];
//        byteBuf.readBytes(data);
//
//        //解码
////        Message message = pkgManager.get(pkgId);//客户端需要获取并保持，直到data数据获取完整后进行拼装
//        PkgInfo pkgInfo = null;
//        pkgInfo = pkgManager.get(pkgId);//客户端需要获取并保持，直到data数据获取完整后进行拼装。服务端只需缓存在内存里，当客户端需要时候给予即可。若是分布式，可考虑存放在redis中
//        pkgInfo.setFrom(fromId);
//        pkgInfo.setTo(toId);
//        pkgInfo.setPkgId(pkgId);
//        pkgInfo.setType(type);
//        pkgInfo.setVersion(version);
//        pkgInfo.setPkgCnt(pkgcnt);
//        pkgInfo.setcPkgn(cpkgn);
//        pkgInfo.addData(data);
//        pkgInfo.setServerReceTime(System.currentTimeMillis());
//        pkgManager.checkPkg();
//        return pkgInfo;
//    }

    public static PkgInfo decodeOne(ByteBuf byteBuf, PkgManager pkgManager) throws UnsupportedEncodingException {
        String fromId = byteBuf.readCharSequence(IMEncoder.ID_LEN, IMEncoder.CODESET).toString();
        String toId = byteBuf.readCharSequence(IMEncoder.ID_LEN, IMEncoder.CODESET).toString();
        String pkgId = byteBuf.readCharSequence(IMEncoder.ID_LEN, IMEncoder.CODESET).toString();

        byte type = byteBuf.readByte();
        byte version = byteBuf.readByte();
        byte pkgcnt = byteBuf.readByte();
        byte cpkgn = byteBuf.readByte();

        short datalen = byteBuf.readShort();
        byte[] data = new byte[datalen];
        byteBuf.readBytes(data);

        PkgInfo pkgInfo = new PkgInfo();
        pkgInfo.setFrom(fromId);
        pkgInfo.setTo(toId);
        pkgInfo.setPkgId(pkgId);
        pkgInfo.setType(type);
        pkgInfo.setVersion(version);
        pkgInfo.setPkgCnt(pkgcnt);
        pkgInfo.setcPkgn(cpkgn);
        pkgInfo.addData(data);
        pkgInfo.setServerReceTime(System.currentTimeMillis());
        return pkgInfo;
    }

    public void addWaitForFinish(WaitForFinish waitForFinish) {
        waitfininsh.add(waitForFinish);
    }

    public void removeWaitForFinish(WaitForFinish waitForFinish) {
        waitfininsh.remove(waitForFinish);
    }

    public void release() {
        check = false;
    }

    public void setPkgInfoConsumer(Consumer<WaitForFinish> consumer) {
        pkgInfoConsumer = consumer;
    }

    public void addWholePkg(WholePkg wholePkg) {
        wholePkgMap.put(wholePkg.getPkgId(), wholePkg);
    }

    public ByteBuf getByteBuf(String pkgId, int i) {
        if (wholePkgMap.containsKey(pkgId)) {
            WholePkg wholePkg = wholePkgMap.get(pkgId);
            return wholePkg.get(i);
        }
        return null;
    }

    public void removeWholePkg(String pkgId) {
//        WholePkg wholePkg = wholePkgMap.get(pkgId);
//        if (wholePkg != null) {
////            wholePkg.release();
//            System.out.println("send pkg stl is :" + wholePkg.getDelta());
//        }
        WholePkg v = wholePkgMap.remove(pkgId);
        if (v != null)
            v.release();
        if (wholePkgMap.size() > 20) {
            System.out.println("---map.size=" + wholePkgMap.size());
        }
    }

    private void checkWholePkgCnt() {
        if (wholePkgMap.size() > 10) {
            for (WholePkg v : wholePkgMap.values()) {
                v.release();
            }
            wholePkgMap.clear();
        }
    }

}
