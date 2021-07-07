package com.server.im.android.clinet;

import com.server.im.android.clinet.sdk.IMOnReceive;
import com.server.im.android.clinet.sdk.IMSdk;
import com.server.im.android.clinet.sdk.codec.IMEncoder;
import com.server.im.android.clinet.sdk.model.PkgInfo;

public class TestClient2 {
    public static void main(String[] args) {

//        String uid = "4c69bacb-cd0c-4ea9-ab02-17ba6816f6c8", toUid = "4c69bacb-cd0c-4ea9-ab02-17ba6816f6c9";
//        String uid = "001", toUid = "002";
        String uid = "001", toUid = "123";
        IMSdk.getInstance().setImOnReceive(new IMOnReceive() {
            @Override
            public void onReceive(PkgInfo pkgInfo) {
                System.out.println(new String(pkgInfo.getData(), IMEncoder.CODESET));
            }
        });

        IMSdk.getInstance().init(toUid, null, () -> {
            IMSdk.getInstance().getChat().send(uid, "哈哈");
        });
        System.out.println("TestClient2");
    }
}
