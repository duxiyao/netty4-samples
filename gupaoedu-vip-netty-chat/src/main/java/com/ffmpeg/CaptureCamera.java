package com.ffmpeg;

import com.alibaba.fastjson.JSONObject;
import com.server.im.android.clinet.sdk.IMOnReceive;
import com.server.im.android.clinet.sdk.IMSdk;
import com.server.im.android.clinet.sdk.codec.IMEncoder;
import com.server.im.android.clinet.sdk.model.PkgInfo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CaptureCamera {
    static {
        System.loadLibrary("myffmpeg");
    }

    static ExecutorService executorService = Executors.newSingleThreadExecutor();

    static String uid = "4c69bacb-cd0c-4ea9-ab02-17ba6816f6c8", toUid = "4c69bacb-cd0c-4ea9-ab02-17ba6816f6c9";

    public native void startCapture();

    private long tmp;

    public void onCalcEnd() {
        long cur = System.currentTimeMillis();
        System.out.println("-------delta=" + (cur - tmp));
    }
    public void onCalcStart() {
        tmp = System.currentTimeMillis();
    }

    public void onGetData(byte[] data) {
//        System.out.println(data.length);
//        System.out.println("send-----------"+Thread.currentThread().getName());
        executorService.execute(() -> {
            IMSdk.getInstance().getChat().sendVideo(toUid, data);
        });
    }

    public static void main(String[] args) {
//        System.out.println("start-----------"+Thread.currentThread().getName());
        JSONObject jsonObject = new JSONObject();
        IMSdk.getInstance().init(uid, null, () -> {

            jsonObject.put("mimeType", "video/avc");
            jsonObject.put("videoWidth", 1280);
            jsonObject.put("videoHeight", 720);
            jsonObject.put("keyBitRate", 800*1000);
            jsonObject.put("fps", 30);
//            jsonObject.put("iFrameInterval", 1);
            String json = jsonObject.toString();
            System.out.println(json);
            IMSdk.getInstance().getChat().sendTransparentTxt(toUid, json);
            new Thread(() -> {
//                System.out.println("start capture-----------"+Thread.currentThread().getName());
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
