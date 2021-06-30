package com.server.im.android.clinet;

import com.server.im.android.clinet.sdk.IMOnReceive;
import com.server.im.android.clinet.sdk.IMSdk;
import com.server.im.android.clinet.sdk.codec.IMEncoder;
import com.server.im.android.clinet.sdk.model.PkgInfo;

public class TestClient1 {
    public static void main(String[] args) {
        String uid = "4c69bacb-cd0c-4ea9-ab02-17ba6816f6c8", toUid = "4c69bacb-cd0c-4ea9-ab02-17ba6816f6c9";
//        String uid = "001", toUid = "002";
        IMSdk.getInstance().init(uid, null, () -> {
            StringBuilder sb = new StringBuilder();
            int i = 0;
            while (sb.length() < 1472 * 5) {
                sb.append(i + "-你好啊c2 ");
                i++;
            }
            System.out.println("cnt " + i);
            IMSdk.getInstance().getChat().send(toUid, sb.toString());
        });
        IMSdk.getInstance().setImOnReceive(new IMOnReceive() {
            @Override
            public void onReceive(PkgInfo pkgInfo) {
                System.out.println(new String(pkgInfo.getData(), IMEncoder.CODESET));
            }
        });
        System.out.println("TestClient1");
    }
}
