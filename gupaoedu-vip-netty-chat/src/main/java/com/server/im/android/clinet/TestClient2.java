package com.server.im.android.clinet;

import com.server.im.android.clinet.audio.DataQueue;
import com.server.im.android.clinet.sdk.IMOnReceive;
import com.server.im.android.clinet.sdk.IMSdk;
import com.server.im.android.clinet.sdk.codec.IMEncoder;
import com.server.im.android.clinet.sdk.model.PkgInfo;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 音频回路单机测试
 */
public class TestClient2 {
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    private static short getShort(byte[] data) {
        short value = 0;

        value |= (data[0] & 0xFF);
        value = (short) (value << 8);
        value |= (data[1] & 0xFF);

        return value;
    }

    private static volatile boolean flag = true;

    public static void main(String[] args) throws InterruptedException {
//region test
//        byte[] ds=new byte[10];
//        Short sht=Short.MAX_VALUE;
//        ByteBuffer buffer=ByteBuffer.allocate(2);
//        buffer.putShort(sht);
//        buffer.flip();
//        buffer.get(ds,0,2);
//        System.out.println("unsint pre="+sht);
//        System.out.println("unsint="+Short.toUnsignedInt(sht));
//        System.out.println(sht&0x0FFFF);
//        sht++;
//
//        buffer.flip();
//        buffer.putShort(sht);
//        buffer.flip();
//        buffer.get(ds,0,2);
//        System.out.println("unsint pre="+sht);
//        System.out.println("unsint="+Short.toUnsignedInt(sht));
//        System.out.println(sht&0x0FFFF);
//        sht++;
//
//        buffer.flip();
//        buffer.putShort(sht);
//        buffer.flip();
//        buffer.get(ds,0,2);
//        System.out.println("unsint pre="+sht);
//        System.out.println("unsint="+Short.toUnsignedInt(sht));
//        System.out.println(sht&0x0FFFF);
//        sht++;
//
//        buffer.flip();
//        buffer.putShort(sht);
//        buffer.flip();
//        buffer.get(ds,0,2);
//        System.out.println("unsint pre="+sht);
//        System.out.println("unsint="+Short.toUnsignedInt(sht));
//        System.out.println(sht&0x0FFFF);
////        System.out.println(-1&0x0FFFF);
//        if(true) return;

//        byte[] a = new byte[2];
//        ByteBuffer buffer = ByteBuffer.wrap(a);
//        buffer.put((byte) 1);
//        buffer.put((byte) 2);
//        buffer.flip();
//        buffer.get(a);
//
//        ByteBuffer inp = ByteBuffer.allocate(2);
//        inp.put(a, 1, 1);


//        byte[] b = new byte[]{1, 2};
//        byte[] a = new byte[9];
//        ByteBuffer buffer = ByteBuffer.wrap(a);
//        buffer.position(7);
//        buffer.put(b, 0, b.length);
//        String s = "";
//
//        LinkedBlockingDeque q = new LinkedBlockingDeque();
//        q.offer(1);
//        q.offer(2);
//        q.offer(3);
//        System.out.println(q.poll());
//        System.out.println(q.take());
//        if (true) return;
//endregion

//        String uid = "4c69bacb-cd0c-4ea9-ab02-17ba6816f6c8", toUid = "4c69bacb-cd0c-4ea9-ab02-17ba6816f6c9";
//        String uid = "001", toUid = "002";
//        String uid = "001", toUid = "123";
        String uid = "123", toUid = "001";
        DataQueue dataQueue = new DataQueue();

        IMSdk.getInstance().setImOnReceive(new IMOnReceive() {
            @Override
            public void onReceive(PkgInfo pkgInfo) {
                switch (pkgInfo.getType()) {
                    case PkgInfo.TYPE_TRANSFER_TXT:
                        System.out.println(new String(pkgInfo.getData(), IMEncoder.CODESET));
                        break;
                    case PkgInfo.TYPE_TRANSFER_AUDIO:
                        byte[] data = pkgInfo.getData();
                        if (dataQueue.size() > 100 && flag) {
                            System.out.println("--------------100");
                            flag = false;
                            new Thread(() -> {
                                List<byte[]> ds = dataQueue.hold();
                                while (!flag) {
                                    try {
                                        ds.forEach(it -> {
                                            IMSdk.getInstance().getChat().sendAudio(uid, it);
//                                            try {
//                                                Thread.sleep(5);
//                                            } catch (InterruptedException e) {
//                                                e.printStackTrace();
//                                            }
                                        });
                                        System.out.println("--------------sended");
                                        TimeUnit.SECONDS.sleep(3);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }).start();
                        } else {
                            if (flag)
                                dataQueue.put(data);
                        }

//                        System.arraycopy();
//                        ByteBuffer buffer = ByteBuffer.wrap(data);
//                        short p = buffer.getShort();
//                        int pkgCnt = pkgInfo.getPkgCnt();
//                        int cPkgn = pkgInfo.getcPkgn();
//                        String st = "";
//                        Long stime = pkgInfo.getServerReceTime();
//
//                        if (stime != null) {
//                            Date rd = new Date(stime);
//                            st = "time:" + simpleDateFormat.format(rd);
//                        }
//                        System.out.println(st + " p=" + p + " pkgCnt=" + pkgCnt + " cPkgn=" + cPkgn + " data.len=" + data.length);
                        break;
                }
            }
        });

        IMSdk.getInstance().init(toUid, null, () -> {
//            IMSdk.getInstance().getChat().send(uid, "哈哈");
        });

        System.out.println("TestClient2");
    }
}
