package com.ffmpeg;

import com.alibaba.fastjson.JSONObject;
import com.server.im.android.clinet.sdk.IMOnReceive;
import com.server.im.android.clinet.sdk.IMSdk;
import com.server.im.android.clinet.sdk.codec.IMEncoder;
import com.server.im.android.clinet.sdk.model.PkgInfo;

public class CaptureCamera {
    static {
        System.loadLibrary("myffmpeg");
    }

    static String uid = "4c69bacb-cd0c-4ea9-ab02-17ba6816f6c8", toUid = "4c69bacb-cd0c-4ea9-ab02-17ba6816f6c9";

    public native void startCapture();

    public void onGetData(byte[] data) {
//        System.out.println(data.length);
        IMSdk.getInstance().getChat().sendVideo(toUid, data);
    }

    public static void main(String[] args) {
        JSONObject jsonObject = new JSONObject();
        IMSdk.getInstance().init(uid, null, () -> {

            jsonObject.put("mimeType", "video/avc");
            jsonObject.put("videoWidth", 1280);
            jsonObject.put("videoHeight", 720);
//            jsonObject.put("keyBitRate", SendAct.KEY_BIT_RATE);
            jsonObject.put("fps", 30);
            jsonObject.put("iFrameInterval", 1);
            String json = jsonObject.toString();
            System.out.println(json);
            IMSdk.getInstance().getChat().sendTransparentTxt(toUid, json);
            new Thread(()->{
                new CaptureCamera().startCapture();
            }).start();
        });
        IMSdk.getInstance().setImOnReceive(new IMOnReceive() {
            @Override
            public void onReceive(PkgInfo pkgInfo) {
//                System.out.println(new String(pkgInfo.getData(), IMEncoder.CODESET));
            }
        });
        System.out.println("Test CaptureCamera");
    }
}
