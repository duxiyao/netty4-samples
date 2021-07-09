package com.server.im.android.clinet;

import com.server.im.android.clinet.sdk.IMOnReceive;
import com.server.im.android.clinet.sdk.IMSdk;
import com.server.im.android.clinet.sdk.codec.IMEncoder;
import com.server.im.android.clinet.sdk.model.PkgInfo;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TestClient2 {
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    public static void main(String[] args) {

//        Short sht=Short.MAX_VALUE;
//        sht++;
//        System.out.println(sht);
//        if(true) return;

//        String uid = "4c69bacb-cd0c-4ea9-ab02-17ba6816f6c8", toUid = "4c69bacb-cd0c-4ea9-ab02-17ba6816f6c9";
//        String uid = "001", toUid = "002";
//        String uid = "001", toUid = "123";
        String uid = "123", toUid = "001";

        IMSdk.getInstance().setImOnReceive(new IMOnReceive() {
            @Override
            public void onReceive(PkgInfo pkgInfo) {
                switch (pkgInfo.getType()) {
                    case PkgInfo.TYPE_TRANSFER_TXT:
                        System.out.println(new String(pkgInfo.getData(), IMEncoder.CODESET));
                        break;
                    case PkgInfo.TYPE_TRANSFER_AUDIO:
                        byte[] data = pkgInfo.getData();
                        int pkgCnt = pkgInfo.getPkgCnt();
                        int cPkgn = pkgInfo.getcPkgn();
                        String st = "";
                        Long stime = pkgInfo.getServerReceTime();

                        if (stime != null) {
                            Date rd = new Date(stime);
                            st = "time:" + simpleDateFormat.format(rd);
                        }
                        System.out.println(st + " pkgCnt=" + pkgCnt + " cPkgn=" + cPkgn + " data.len=" + data.length);
                        break;
                }
            }
        });

        IMSdk.getInstance().init(toUid, null, () -> {
            IMSdk.getInstance().getChat().send(uid, "哈哈");
        });

        System.out.println("TestClient2");
    }
}
